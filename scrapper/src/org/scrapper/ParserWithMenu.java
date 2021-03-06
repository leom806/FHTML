package org.scrapper;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import static org.scrapper.Builder.*;

/**
* Name: ParserWithMenu
 Date: 15-12-2016
 Update: 14-01-2017
 Description: Conversion object class.
*/
public abstract class ParserWithMenu implements Parser{

    /*
    * =========================    VARIABLES    ================================
    */

    private boolean status = false, display = false;
    private String aim = null, nextOption = null, searchQuery = null, content = null;
    private String[] allOptions = null;
    private Document code = null, doc = null;

    /*
    * =========================    CONSTANTS    ================================
    */
    
    private String tag = null; // mainly marcation tag in the page 
    private String mainTag = null; // tag and id or class that indicates main titles in the page
    private String contentTag = null; // id or class that indicates the content to scrap
    private String source = null; // Website to scrap
 
    /*
    * =========================    MESSAGES    =================================
    */
    
    private final String INIT_MESSAGE = " JScrapper "+Parser.VERSION+".";
    private final String PARSING_ERROR_MESSAGE = "Error in scrapping proccess.";
    private final String NOT_FOUND_MESSAGE = "Not found.";
    private final String NULL_ARGS_ERROR = "Should not use null arguments here.";
    private final String CONNECTION_FAILED_MESSAGE = "Could not connect to the page.";
    private final String ALL_OPTIONS_MESSAGE = "See All";
    private final String NOT_CONNECTED_MESSAGE = "Parser is not connected to the page.";
    private final String UNKNOW_ERROR_MESSAGE = "Unknow error.";
    
    /*
    * =========================    CONSTRUCTORS    =============================
    */

    public ParserWithMenu() {
        this(false, false);
    }

    public ParserWithMenu(boolean status, boolean display) {
        print(INIT_MESSAGE);
        this.status = status;
        this.display = display;
    }

    /*
    * =====================    GETTERS & SETTERS    ============================
    */

    public String[] getOptions() {
        if (allOptions != null)
            return allOptions;
        return new String[]{NOT_FOUND_MESSAGE};
    }
    
    public String getTag() {
        return tag;
    }
    
    public String getMainTag() {
        return this.mainTag;
    }
    
    public String getContentTag() {
        return this.contentTag;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setTag(String tag) {
        if(tag == null)
            throw new RuntimeException("Tag must not be null.");
        this.tag = tag;
    }
    
    public void setMainTag(String tag) {
        if(tag == null)
            throw new RuntimeException("Main tag must not be null.");
        this.mainTag = tag;
    }
    
    public void setContentTag(String tag) {
        if(tag == null)
            throw new RuntimeException("Content tag must not be null.");
        this.contentTag = tag;
    }
    
    public void setSource(String url) {
        if(url == null)
            throw new RuntimeException("Source must not be null.");
        this.source = url;
    }
    
    /*
    * =====================    ABSTRACT METHODS    =============================
    */    
    
    /**
     * This method MUST initialize all tags as well the source url.
     */
    public abstract void initTagsAndSource();

    /*
    * ===========================    METHODS    ================================
    */

    /**
     * Mainly parse method.
     *
     * @param code
     * @return
     */
    public String MainParseMethod(String code) {
        if(status) {
            print("> Main method.");
        }
        if(display) print(Title(doc)+"\n");

        Whitelist wl = new Whitelist();
        wl.addTags(getTag(), "p");

        // Clean using the allowed tags of the whitelist
        String cleanCode = Jsoup.clean(code, wl);

        cleanCode = RemoveUnnecessaryThings(cleanCode);

        String ocurrency = "<"+getTag()+">"+aim+"</"+getTag()+">";
        String limit = "<"+getTag()+">"+nextOption+"</"+getTag()+">";

        cleanCode = cleanCode.substring(cleanCode.lastIndexOf(ocurrency)+ocurrency.length());

        if(cleanCode.contains(limit))
            cleanCode = cleanCode.substring(0, cleanCode.indexOf(limit));
        
        content = clear(cleanCode);
        
        if(display) print(content);

        return content;
    }
    
    public abstract String RemoveUnnecessaryThings(String code);

    /**
     * Alternatively parse method, used if main fails; less accurate.
     *
     * @param code
     * @return
     */
    public String AlternativeParseMethod(String code) {
        try {
            if(code.contains(aim) && code.contains("</p>")) {
                if(status) {
                    print("> Alternative method.");
                }
                String minified = code.substring(code.indexOf(aim), code.length());
                code = minified.substring(0, minified.indexOf("</p>"));
                content = clear(code); // Clear tags html off the code.
                
                if(display) print(Title(doc)+"\n");
                if(display) print(content+"\n");
            }
        }catch(Exception ev) {
            print(ev.getMessage()+"\n", "red");
            return PARSING_ERROR_MESSAGE;
        }

        return content;
    }

    /**
     * Used when the user selects the first option; brings up all content of the page.
     *
     * @return
     */
    public String LastParseMethod() {
        if(status) {
            print("> Last method.");
        }
        if(display) print(Title(doc)+"\n");

        content = doc.select(getContentTag()).select("p").toString().replace("\n", "ʘ");
        content = clear(content).replace("ʘ", "\n");

        if(display) print(content);

        return content;
    }

    /**
     * Parser core method.
     *
     * @return
     * @throws NullPointerException
     * @throws Exception
     */
    public String Core() throws NullPointerException, Exception{

        // Show options dialog
        try {
            // Options selection dialog
            Object[] opcoes = Options(doc);
            aim = get("Options:", opcoes);
            int i = 0;
            for(Object opcao : opcoes) {
                if(opcao.equals(aim)) break;
                else i++;
            }
            if(!aim.equals(opcoes[opcoes.length-1]))
                nextOption = opcoes[i+1].toString().replace(" - ", "");
            if (status) print("> Searching:"+aim);

        }catch (NullPointerException ex) {
            throw new NullPointerException("Core Error\n"+NULL_ARGS_ERROR);
        }

        // ParserWithMenu only if verification returns true
        if(Verify()) {

            if(status) print("> Verified.");
            if(status) print("> Showing options. ");

            if(display) {
                print(); // new line
                for(String opcao : Options(doc)) {
                    print(opcao);
                }
            }

            // Gets p tags
            Elements tags = doc.body().getElementsByTag("p");
            String text = tags.toString();

            // Gets content 
            String all = doc.body().select(getContentTag()).toString();

           /**
            * Scrapping core process.
            */

           if(!aim.equals(ALL_OPTIONS_MESSAGE)) {

                if(all.contains(aim)) {
                    /*
                    * Principal processo de raspagem. Usa as opcoes para buscar.
                    *
                    * Gera um novo código limpo por uma Whitelist do Jsoup.
                    * Cria substring partindo do titulo escolhido e retirando caracter que
                    * pode gerar erro na próxima substring.
                    * Cria a última substring usando o próximo item como limite.
                    */

                    content = MainParseMethod(all);
                }else{
                    /*
                    * Processo de raspagem alternativo. Usa parte do texto para buscar.
                    *
                    * Primeiro é cortado da busca até o final. Depois pegamos esse corte
                    * e limitamos até a próxima tag. Então converte-se novamente com o Jsoup
                    * para poder usar o método text().
                    */

                    content = AlternativeParseMethod(text);
                }

            }else{
               content = LastParseMethod();
            }

            if(content != null)
                return content;
        }

        throw new Exception(PARSING_ERROR_MESSAGE);

    }

    /**
     * Gets options in the page.
     *
     * @param doc
     * @return
     */
    public String[] Options(Document doc) {
        String[] options = doc.select(getMainTag()).toString().split("\n");
        allOptions  = new String[options.length+1];
        allOptions[0] = " - "+ALL_OPTIONS_MESSAGE;
        for(int i = 0; i < options.length; i++) {
            allOptions[i+1] = " - "+clear(options[i]);
        }
        return allOptions;
    }

    /**
     * Connects to the webpage.
     *
     * @param url
     * @return
     * @throws java.lang.Exception
     * @throws java.io.IOException
     */
    public boolean Initialize(String url) throws Exception, IOException{
        try {
            code = connect(url);
            return true;
            
        } catch (IOException ex) {
            throw new IOException("Initializing error\n"+CONNECTION_FAILED_MESSAGE);
            
        } catch(Exception ev) {
            throw new Exception("Initializing error\n"+UNKNOW_ERROR_MESSAGE);
        }
    }

    /**
     * Verify the code.
     *
     * @return
     * @throws java.lang.Exception
     */
    public boolean Verify() throws Exception, NullPointerException{

        aim = aim.replace(" - ", "");

        try {
            boolean verify_code = code.toString().isEmpty();
        }catch(NullPointerException ex) {
            throw new NullPointerException("Verify error\n"+NULL_ARGS_ERROR);
        }

        if(aim == null) {
            throw new NullPointerException("Verify error\n"+NULL_ARGS_ERROR);
        }

        if(getSource() == null) {
            throw new NullPointerException("Verify error\n"+NULL_ARGS_ERROR);
        }

        if(!code.toString().contains(aim) && !aim.equals(ALL_OPTIONS_MESSAGE)) {
            throw new Exception("Verify error\n"+NOT_FOUND_MESSAGE);
        }

        return true;
    }
    
    /*
    * ======================    OVERRIDEN METHODS    ===========================
    */
    
    /**
     * Prepares and calls the Core of the scrap process
     *
     * @param query
     * @return
     */
    @Override
    public String Parse(String query){

        // Runs the scrapping core
        try{
            searchQuery = query;
            if(Initialize(getSource()+searchQuery)){  // throws IOException
                if(status) print("> Initialized.");
                if(status) print("> Source: "+getSource());
                // Gets the code and parse it
                doc = Jsoup.parse(code.toString());
                return Core(); 
            }else{
                return NOT_FOUND_MESSAGE;
            }
            
        }catch(IOException ex){
            throw new RuntimeException("Parsing error\n"+NOT_CONNECTED_MESSAGE);
            
        }catch(NullPointerException ex){
            throw new RuntimeException("Parsing error\n"+NULL_ARGS_ERROR);
            
        }catch(final Exception ex){
            throw new RuntimeException("Parsing error\n"+UNKNOW_ERROR_MESSAGE);  // rethrow 
        }
    }
    
    /**
     * Gets the webpage title.
     *
     * @param code
     * @return
     */
    @Override
    public String Title(Document code){
        if (code != null)
            return code.title();
        else 
            return NULL_ARGS_ERROR;
    }

}
