/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk;

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.concurrent.*

/**
 *
 */
class CommandRun implements Runnable {
    def command
    
    public void run(){
        println "Performing ${command}"
        def proc = command.execute()
        proc.consumeProcessOutput()

        if( proc.waitFor()!=0 ){
            System.err.println("Error while performing ${command}")
            System.exit(1)
        }
    }
}

/**
 *
 * @author victor
 */
public class Utils {

    protected static String WGET_COMMAND = "wget -nv --no-proxy"


    /**
     *
     * @param inputFile
     */
    public static void gzipFile( String inputFile ){
        try {
            // Create the GZIP output stream
            String outFilename = inputFile+".gz";
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFilename));

            // Open the input file
            FileInputStream inp = new FileInputStream(inputFile);

            // Transfer bytes from the input file to the GZIP output stream
            byte[] buf = new byte[4096];
            int len;
            while ((len = inp.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            inp.close();

            // Complete the GZIP file
            out.finish();
            out.close();
        } catch (IOException e) {
        }
    }


    /**
     * create a buffered reader for a file checkin first if the file is gzipped
     *
     * @param file : a file path, if file doesnt start with '/' is treated as a resource
     * @return
     */
    public static BufferedReader createReader(String file) throws IOException, URISyntaxException{

        if( !file.startsWith(File.separator) ){
            //load as resource
            return new BufferedReader(
                    new InputStreamReader( getClass().getResourceAsStream('/'+file)) );
        }

        return createReader(new File(file));
    }

    public static BufferedReader createReader(File file) throws IOException{
        BufferedReader reader = null;

        if( file.getName().endsWith(".gz") ){
            reader=new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))) );
        }
        else{
            reader=new BufferedReader(new FileReader(file));
        }

        return reader;
    }

    
    /**
     *
     */
    static def createInputStream(file){
        if( file instanceof String ){
            file = new File(file)
        }
        
        if( file.name.endsWith('.gz') )
            return new GZIPInputStream(new FileInputStream(file))
        else
            return new FileInputStream(file)
    }
    
    
    /**
     * untar the content of a .tar.gz file to a single file
     */
    static public boolean untarToFile( String fileIn, String fileOut ){

        println "Untar ${fileIn}."

        def output = new BufferedOutputStream(new FileOutputStream( fileOut ))
        def proc="tar -zxO -f ${fileIn}".execute()
        proc.consumeProcessOutputStream(output)

        if( proc.waitFor()!=0 ){
            System.err.println("Error: untar ${fileIn}.")
            return false
        }

        output.close()

        return true
    }
    
    static public InputStream untarToStream( String fileIn ){
        def proc="tar -zxO -f ${fileIn}".execute()
        return proc.getIn()
    }


    /**
     * utility method, create a directory if not exists
     */
    static public boolean createDir(String root){
        File f = new File(root)

        //create out dir
        if( f.exists() ){
            if( !f.isDirectory() ){
                System.err.println("Error: Base dir (${root}) is not a directory.")
                return false
            }
        }
        else if( !f.mkdirs() ){
            System.err.println("Error: Cannot create base dir (${root}).")
            return false
        }

        println "Created ${root} base directory."

        return true
    }

    /**
     * closure that compares two chromosomes names (chrA , chrB). Converts
     * A, B to integer and compares numerically if possible
     */
    static public def compChr = { a, b ->
        String a_str = a
        if( a.toLowerCase().startsWith('chr') )
            a_str = a.substring(3)

        String b_str = b
        if( b.toLowerCase().startsWith('chr') )
            b_str = b.substring(3)

        try{
            return (a_str as Integer)<=>(b_str as Integer)
        } catch(Exception e){}
            
        return a_str<=>b_str
    }
    
    
    /**
     * download a file using wget
     */
    static def download(String urlSrc, String urlDst=null){
        String target = urlDst ? "-O ${urlDst}" : ''
        def p = "${WGET_COMMAND} ${target} ${urlSrc}".execute()
        
        if(p.waitFor()!=0){
            "rm ${urlDst ?: ''}".execute()//remove empty output file
            return false
        }
        
        return true
    }
    
    /**
     * get File, if not exists checks the compressed '.gz' file name
     */
    static File getFileIfCompress(String name){
        def file = new File(name)

        if( !file.exists() )
            file = new File(name+'.gz')
        else
            return file

        if( !file.exists() )
            return null

        return file
    }
    
    /**
     * count file lines
     */
    static int countLines(file){
        //get read count
        def p="wc -l ${file}".execute()

        if( p.waitFor()==0 ){
            def reader = new BufferedReader(new StringReader(p.text))
            return reader.readLine().split("\\s")[0] as Integer
        }
        
        return -1
    }
    
    /**
     *
     */
    static runClosures(list, int threads){
        def pool = Executors.newFixedThreadPool(threads)
        list.each{ pool.execute(it) }
        pool.shutdown()
        
        //wait all threads to finish
        while( !pool.isTerminated() ){
            try {
                Thread.sleep(1000)
            } catch (ex) { }
        }
    }
    
    /**
     *
     */
    static runCommands(list, int threads){
        def pool = Executors.newFixedThreadPool(threads)
        list.each{ pool.execute(new CommandRun(command:it)) }
        pool.shutdown()
        
        //wait all threads to finish
        while( !pool.isTerminated() ){
            try {
                Thread.sleep(1000)
            } catch (ex) { }
        }
    }
    
}

