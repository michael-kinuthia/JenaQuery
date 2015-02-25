/*
 * Author: Michael Kinuthia
 * Project Details: Endpoint Servive to query using Jena API
 */
package com.michael.jenaquery;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class jenaservlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    String sparqlresults = ""; 
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {         
        //Output page containg the results and the user input
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Start Page</title>"); 
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            out.println("<link rel='stylesheet' type='text/css' media=\"all\" href='style.css'/>");
            out.println("</head>"); 
            out.println("<body>"); 
            out.println("<form method=\"post\" action=\"jenaservlet\">");
            out.println("<div>");
            out.println("<div id=\"divHeader\">");
            out.println("<h1>Query Using Jena Api</h1>");
            out.println("</div>");
            out.println("<div id=\"divAlign\">");
            out.println("<div id=\"divTitle\">");
            out.println("URL of File:<input type=\"text\" name=\"userURL\" value="+request.getParameter("userURL")+"><br /><br />");
            out.println("</div>");
            out.println("</div>");
            out.println("<div id=\"divAlign\">");
            out.println("<div id=\"divTitle\">");
            out.println("Rules Supported:<textarea disabled rows=\"4\" cols=\"50\" name=\"rules\">");
            out.println("1. Select");
            out.println("2. Describe");
            out.println("3. Construct");
            out.println("4. Ask");
            out.println("</textarea><br /><br />");
            out.println("</div>");
            out.println("</div>");
            out.println("<div id=\"divAlign\">");
            out.println("<div id=\"divTitle\">");
            out.println("SPAQL query:<textarea rows=\"4\" cols=\"50\" name=\"sparqlquery\">"+ request.getParameter("sparqlquery")+"</textarea><br /><br />");
            out.println("</div>");
            out.println("</div>");
            out.println("<div id=\"divAlign\">");
            out.println("<div id=\"divinput\">");
            out.println("<input type=\"submit\" value=\"Query\">   <br /><br />");
            out.println("</div>");
            out.println("</div>");
            out.println("<div id=\"divAlign\">");
            out.println("<div id=\"divTitle\">");
            out.println("Query Results:<textarea disabled rows=\"10\" cols=\"80\" name=\"sparqlresults\">"+ sparqlresults+"</textarea><br /><br />");
            out.println("</div>");
            out.println("</div>");
            out.println("</form>");
            out.println("</body>");            
            out.println("</html>");           
        }       
    }
    /**
     * Handles the userInput, process the HTTP requests and responses
     * Displays the output to the user in html format.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {       
        //Initialize variables used in the servlet
        String url= request.getParameter("userURL");                //Capture user entered URL       
        String sparqlquery = request.getParameter("sparqlquery");   //Capture user entered Query
        
        StringWriter WriteOut = new StringWriter();       
        //Create Ontology model from the URL that the user has inputed 
        OntModel model  = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        model.read(url, "RDF/XML"); 
        //Create a Queryfactory from the user input query 
        String querystring = sparqlquery;
        Query Jenaquery = QueryFactory.create(querystring);
        // if statement Queries Jena Server depending on the Query Request and puts output in the output variable
        //Select Query
        if(Jenaquery.isSelectType()){
            QueryExecution JenaExecution = QueryExecutionFactory.create(Jenaquery, model);
            ResultSet results = JenaExecution.execSelect();            
            ByteArrayOutputStream OutputArray = new ByteArrayOutputStream();
            ResultSetFormatter.out(OutputArray, results);
            sparqlresults = OutputArray.toString();           
            JenaExecution.close();
        //Describe Query
        }else if(Jenaquery.isDescribeType()){
            QueryExecution JenaExecution = QueryExecutionFactory.create(Jenaquery, model);
            Model results = JenaExecution.execDescribe();
            results.write(WriteOut);
            sparqlresults = WriteOut.toString();
            JenaExecution.close();
        //Construct Query
        }else if(Jenaquery.isConstructType()){
            QueryExecution JenaExecution = QueryExecutionFactory.create(Jenaquery, model);
            Model results = JenaExecution.execConstruct();            
            results.write(WriteOut, "TURTLE");
            sparqlresults = WriteOut.toString();
            JenaExecution.close();
        //Ask Query
        }else if(Jenaquery.isAskType()){
            QueryExecution JenaExecution = QueryExecutionFactory.create(Jenaquery, model);
            boolean results = JenaExecution.execAsk();
            ResultSetFormatter.outputAsRDF(sparqlresults, results);
            JenaExecution.close();
        }       
        processRequest(request, response);
    }    
    //Returns a short description of the servlet.      
    @Override
    public String getServletInfo() {
        String Description = "This Servlet handles query request of Select, Describe, Construct and Ask of Jena Server.";
        return Description;
    }

}
