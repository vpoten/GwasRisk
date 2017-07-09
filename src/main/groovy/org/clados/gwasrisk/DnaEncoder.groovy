/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

/**
 * Class that stores a DNA sequence in a compressed form: triple->byte
 *
 * @author victor
 */
class DnaEncoder {

    byte [] buffer
    int size
    
    static final Character SPACE_CHAR = ' ' as Character
    protected static final def symbols = ['A','G','T','C','?',SPACE_CHAR]
    protected static encodeMap = [:] as TreeMap
    protected static decodeMap = [:] as TreeMap
     
    static{
        generateTables()
    }
    
    /**
     *
     */
    public DnaEncoder(String seq){
        encode(seq)
    }
    
    /**
     *
     */
    protected static generateTables(){
        byte code = 0;
        
        (0..(symbols.size()-1)).each{ i->
            (0..(symbols.size()-1)).each{ j->
                (0..(symbols.size()-1)).each{ k->
                    String seq = "${symbols[i]}${symbols[j]}${symbols[k]}"
                    setEncodeMap( [seq.charAt(0),seq.charAt(1),seq.charAt(2)] as char [], code)
                    decodeMap[code] = seq
                    code = code+1
                }
            }
        }
    }
    
    /**
     *
     */
    int length(){
        return size
    }
    
    /**
     *
     */
    private byte getEncodeMap(char [] triple){
        return encodeMap[triple[0]][triple[1]][triple[2]]
    }
    
    /**
     *
     */
    static private void setEncodeMap(char [] triple, byte value){
        if( !encodeMap[triple[0]] )
            encodeMap[triple[0]] = [:]
            
        if( !encodeMap[triple[0]][triple[1]] )
            encodeMap[triple[0]][triple[1]] = [:]
            
        encodeMap[triple[0]][triple[1]][triple[2]] = value
    }
    
    /**
     *
     */
    void encode(String seq){
        size = seq.length()
        int cont =0
        buffer = new byte [(int)Math.ceil(size/3.0)]
        char[] triple = new char [3]
        
        for(int i=0; i<size; i+=3){
            triple[0] = seq.charAt(i)
            triple[1] = ((i+1)>=size) ? SPACE_CHAR : seq.charAt(i+1)
            triple[2] = ((i+2)>=size) ? SPACE_CHAR : seq.charAt(i+2)
                
            buffer[cont++] = getEncodeMap(triple)
        }
    }
    
    /**
     *
     */
    String decode(){
        StringBuffer str = new StringBuffer()
        
        for(int i=0; i<buffer.length; i++){
            str << decodeMap[buffer[i]]
        }
        
        return str.toString().trim()
    }
    
    /**
     *
     */
    char charAt(int idx){
        return decodeMap[buffer[(idx/3) as Integer]].charAt((idx%3) as Integer)
    }
    
    /**
     *
     */
    boolean equals(String seq){
        int seqsize = seq.length()
        int cont = 0
        char[] triple = new char [3]
        
        for(int i=0; i<seqsize; i+=3){
            triple[0] = seq.charAt(i)
            triple[1] = ((i+1)>=seqsize) ? SPACE_CHAR : seq.charAt(i+1)
            triple[2] = ((i+2)>=seqsize) ? SPACE_CHAR : seq.charAt(i+2)
                
            if( getEncodeMap(triple) != buffer[cont++] )
                return false
        }
        
        return true
    }
    
}

