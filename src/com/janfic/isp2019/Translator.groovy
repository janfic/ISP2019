package com.janfic.isp2019

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/*
*   Translates files of USER into Map objects that contain a translation into Groovy
*/
class Translator {
    
    //Tools
    private static File file
    private static Binding binding
    private static GroovyShell shell
    private static GroovyClassLoader loader
    private static CompilerConfiguration config
    private static ImportCustomizer imports
    
    //Translated Items
    private static Map translation = [name:"", type:"", pack:""]
    
    //Static initialization of Tools
    static {
        config = new CompilerConfiguration()
        imports = new ImportCustomizer()
        binding = new Binding()
        config.addCompilationCustomizers(imports)
    }
    
    /**
    *   Set the current USER file to be translated. Also resets the translation
    */
    public static void setFile( File f ) {
        file = f
        translation = [name:"",type:"", pack:""]
        translation.name = file.name.substring(0, file.name.lastIndexOf('.'))
    }
    
    /**
    *   Adds an import to the current imports.
    */
    public static void addImport(String alias, String name) {
        imports.addImport(alias, name)
    }
    
    /**
    *   Compiles the current USER file
    */
    public static void compile() {
        makeBinding()
        loader = new GroovyClassLoader(new GroovyClassLoader(), config) //ugh
        shell = new GroovyShell(loader, binding, config)
        shell.evaluate(file)
    }
    
    /**
    *   Sets up the current Binding, which is the main object in charge of the translation. Syntax, Structure, and Keywords are established here
    */
    public static void makeBinding() {
        //creates the 'script' keyword
        binding.script = {
            translation.type = "Script"
        }
        
        //creates the 'component' keyword
        binding.component = {
            translation.type = "Component"
        }
        
        //creates a valid phrase for the name of the definition in USER
        binding."${translation.name}" = { Closure body -> 
            
            // creates the 'fields' keyword and its structure
            body.fields = { Map map ->
                translation.given = translation.fields = map
            }
            
            // makes 'given' have the same effect as 'fields'
            body.given = body.fields 
            
            //creates the 'run' keyword and its following required structure
            body.run = {Closure b -> 
                translation.body = extractScript()
            }
            
             //creates the 'defaults' keyword and its following required structure
            body.defaults = { Map map ->
                translation.defaults = map
            }
            
            //runs the body
            body()
            return body
        }
        
        //creates the 'pack' keyword and required input
        binding.pack = { String p ->
            translation.pack = p
        }
    }
    
    /**
    *   Extracts the runnable script from the file by extracting it from the String version of the file.
    */
    public static String extractScript() {
        String contents = file.text
        int bracePairs = 0
        int first = contents.indexOf("run {") + 4
        int index = first
        while(bracePairs >= 0) {
            index++
            if(contents.charAt(index) == '{') {
                bracePairs++
            }
            else if(contents.charAt(index) == '}') {
                bracePairs--
            }
        }
        return contents.substring(first + 1, index).trim()
    }
    
    /**
    *   Returns the translation
    */
    public static Map getTranslation() {
        return translation
    }
}