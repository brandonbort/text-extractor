/*
 * AUTHOR: Brandon Bort
 * STUDENT_ID: R10454784
 * TITLE: Project 1
 * FOR: CS 2365 Section 002 Spring 2020
 * DESCRIPTION: Single class application to remove html content from a htmml or text file and output the
 * the read text to a separate txt file
 */

//FUNCTIONS:
//    NAME: main
//    PURPOSE: calls the getFile() function to get the user selected file, removes <br /> tags from the read
//        line, also checks if the read line is a <td> or <th> tagged line, also writes to output.txt
//    VARIABLES: 
//        File readFile: the file that user selects on prompt
//        LinkedList stringList: a linked list that will hold the strings read from the file after
//                the html tags are removed
//        Scanner scan: scanner object to read from the readFile
//        String prev: a string that contains the line before the current one, if it exists, for comparison
//        String curr: a string that contains the current read line from the file
//        PrintStream outStream: a PrintStream object that that writes to output.txt
//FUNCTIONS:
//    NAME: getFile()
//    PURPOSE: opens a JFileChooser that allows the user to select a file, and returns it
//    VARIABLES: 
//        File file: the file that user selects on prompt
//        JFileChooser fileChoose: a JFileChooser object that allows the user to select a file 
//        FileNameExtensionFilter filter: a filter that limits the JFileChooser to only display directories,
//                            html, and txt files
//        int result: the id of the button the user selected in the JFileChooser
//FUNCTIONS:
//    NAME: removeHtmlTags
//    PURPOSE: accepts a String preRemove, and removes any html tags in preRemove through recursive calls
//    VARIABLES: 
//        String preRemove: a String object that is passed into the function
//        int openIndex: an int that holds the index of the opening html tag
//        int closeIndex: an int that holds the index of the closing html tag
//        String ret: a temporary string used for extracting titles from img tags
//        String result: a string that holds and returns as the final result
//FUNCTIONS:
//    NAME: replaceEntities
//    PURPOSE: replaces entities outside of html tags with their relevant character
//    VARIABLES: 
//        String htmlString: string passed into the function, that will also be returned
//                            after replacing all entities with their related character
        
package bortbrandontextextractor;

import java.io.File;
import java.util.Scanner;
import java.io.PrintStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileNotFoundException;
import java.util.LinkedList;

public class BortBrandonTextExtractor {

    public static void main(String[] args) throws FileNotFoundException {
        
        File readFile;
        LinkedList<String> stringList = new LinkedList<String>();
        
        //calls the getFile function which opens a file explorer for the user to select
        //a file
        readFile = getFile();
        
        Scanner scan = new Scanner(readFile);
        String prev = new String();
        String curr = new String();
        
        while(scan.hasNextLine()){
            //while the next line begins with <th> or <td>, add the String and a comma to the list
            if(curr != null){
                prev = curr;                
            }
            curr = scan.nextLine();
            //removes void tags and replaces them with new line characters and trims trailing/leading
            //whitespace
            if(curr.contains("<br />")) curr = curr.replaceAll("<br />\\s", "\n").trim();
            
            //handles table rows
            if(curr.contains("<td>") || curr.contains("<th>")){
                //checks if the current line is the end of the table row
                //otherwise, adds commas in between each cell and checks the next lines
                while(curr.trim().equals("</tr>") == false){
                    if(removeHtmlTags(prev).isEmpty() == false){
                        curr = removeHtmlTags(prev) + ',' + removeHtmlTags(curr);
                    }
                    else curr = removeHtmlTags(curr);
                    prev = curr;
                    curr = scan.nextLine();
                }
                //adds the created line to the linked list as long is it is not empty
                if(prev.trim().equals("") == false) stringList.add(prev);
            }
            //this happens after checking for rows, so anything in <tr> tags will not be added
            //multiple times
            curr = removeHtmlTags(curr);
            //checks to make sure
            if(curr.trim().equals("") == false) stringList.add(curr);
        }
        
        //pops all the available elements off of the Linked List and writes them to output.txt
        PrintStream outStream = new PrintStream("output.txt");
        while(stringList.isEmpty() == false){
            outStream.println(stringList.pop());
        }    
        outStream.close();
    }
    
    public static File getFile() throws FileNotFoundException{
        //opens a file explorer that allows the user to select which file to extract
        File file = new File("");
        JFileChooser fileChoose = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML and Text files",
                                                 "html", "txt");
        fileChoose.setFileFilter(filter);
        fileChoose.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChoose.showOpenDialog(fileChoose);
        if(result == JFileChooser.APPROVE_OPTION){
            file = fileChoose.getSelectedFile();
        }

        return file;
    }
    
    public static String removeHtmlTags(String preRemove){
        String result = new String();
        //ints the whole the index of the opening and closing HTML tags
        int openIndex, closeIndex;
        //remove non functional whitespace in the String arguement
        preRemove = preRemove.trim();
        if(preRemove.length() < 1) return "";
        //will replace any html entities within the String ie. &amp with &
        preRemove = replaceEntities(preRemove);
        //since this is a recursive function, will return the argument if there are no html tags
        if(preRemove.contains("<") == false) return preRemove;
        //this will extract the title from <img> tags, if one exists
        if(preRemove.trim().startsWith("<img")){
            if(preRemove.contains("alt=")){
                String ret;
                //index is +5 to disregard the alt="
                int ind = preRemove.indexOf("alt=", 0)+5;
                ret = preRemove.substring(ind, preRemove.indexOf("\"", ind));
                return removeHtmlTags(ret);
            }
        }
        if(preRemove.substring(0, 1).equals("<")){
            if(preRemove.lastIndexOf('<') > -1){
                //sets openIndex to the index of the end of the opening tag + 1 to start past it in substring
                openIndex = preRemove.indexOf('>', 0)+1;
                //finds the closing tag, proper HTML the closing tag should always begin with the last index of <
                closeIndex = preRemove.lastIndexOf('<');
                
                //if the openIndex < closeIndex, that means the line was just the HTML tag
                if(openIndex < closeIndex) result = preRemove.substring(openIndex, closeIndex);
                else return "";
                
            }  
        }        
        //recursive call to the function to remove any remaining html tags
        return removeHtmlTags(result.trim());
    }
    
    //this function replaces any entities found within the string with their character equivalent
    public static String replaceEntities(String htmlString){
        htmlString = htmlString.replaceAll("&amp;", "&");
        htmlString = htmlString.replaceAll("&#38;", "&");
        htmlString = htmlString.replaceAll("&#160;", " ");
        htmlString = htmlString.replaceAll("&nbsp;", " ");
        htmlString = htmlString.replaceAll("&lt;", "<");
        htmlString = htmlString.replaceAll("&#60;", "<");
        htmlString = htmlString.replaceAll("&gt;", ">");
        htmlString = htmlString.replaceAll("&#62;", ">");
        htmlString = htmlString.replaceAll("&quot;", "\"");
        htmlString = htmlString.replaceAll("&#34;", "\"");
        htmlString = htmlString.replaceAll("&apos;", "'");
        htmlString = htmlString.replaceAll("&#39;", "'");
        htmlString = htmlString.replaceAll("&cent;", "¢");
        htmlString = htmlString.replaceAll("&#162;", "¢");
        htmlString = htmlString.replaceAll("&pound;", "£");
        htmlString = htmlString.replaceAll("&#163;", "£");
        htmlString = htmlString.replaceAll("&yen;", "¥");
        htmlString = htmlString.replaceAll("&#165;", "¥");
        htmlString = htmlString.replaceAll("&euro;", "€");
        htmlString = htmlString.replaceAll("&#8364;", "€");
        htmlString = htmlString.replaceAll("&copy;", "©");
        htmlString = htmlString.replaceAll("&#169;", "©");
        htmlString = htmlString.replaceAll("&reg;", "®");
        htmlString = htmlString.replaceAll("&#174;", "®");
        
        return htmlString;
    }
}

