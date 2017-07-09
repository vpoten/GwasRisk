/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk.parser

import groovy.xml.MarkupBuilder

/**
 *
 * @author victor
 */
class TableWriter {

    String tableClass = '' // table CSS class
    String caption = ''
    boolean rowHeader = false // true if the first column of tbody rows are headers

    List thead = []
    List tbody = []
    List tfoot = []

    
    /**
     * writes a xml document which contains the list of tables
     */
    static write(writer, List tables){
        def xml = new MarkupBuilder(writer)
        xml.div(){
            tables.each{ tableobj ->
                table( 'class':tableobj.tableClass ) {
                    caption(tableobj.caption)

                    if( tableobj.thead )
                        thead(){
                            tableobj.thead.each{ row->
                                tr(){ row.each{ th(it) } }
                            }
                        }

                    if( tableobj.tbody )
                        tbody(){
                            tableobj.tbody.each{ row->
                                tr(){
                                    row.eachWithIndex{ c, i->
                                        if(i==0 && tableobj.rowHeader ){ th(c) }
                                        else { td(c) }
                                    }
                                }
                            }
                        }

                    if( tableobj.tfoot )
                        tfoot(){
                            tableobj.tfoot.each{ row->
                                tr(){
                                    row.eachWithIndex{ c, i->
                                        if(i==0 && tableobj.rowHeader ){ th(c) }
                                        else { td(c) }
                                    }
                                }
                            }
                        }
                }//end table
            }
        }//end root
    }

}

