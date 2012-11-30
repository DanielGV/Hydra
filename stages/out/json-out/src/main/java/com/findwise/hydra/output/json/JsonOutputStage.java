package com.findwise.hydra.output.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.findwise.hydra.common.Document.Action;
import com.findwise.hydra.common.Logger;
import com.findwise.hydra.common.SerializationUtils;
import com.findwise.hydra.local.LocalDocument;
import com.findwise.hydra.stage.AbstractOutputStage;
import com.findwise.hydra.stage.Parameter;
import com.findwise.hydra.stage.RequiredArgumentMissingException;
import com.findwise.hydra.stage.Stage;

/**
 * 
 * @author daniel.gomez
 *
 */
@Stage(description="Writes documents to Solr")
public class JsonOutputStage extends AbstractOutputStage {

	@Parameter(required = true, description="The path of the folder to which this stage will post data")
	private String outputPath;
	@Parameter(description="If set, fieldMappings will be ignored and all fields will be sent to Solr.")
	private boolean sendAll = false;
	@Parameter(required = true, description="Identification field of the document, used as file name in output folder.")
	private String idField = "id";
	@Parameter
	private File outputFolder;

	@Override
	public void output(LocalDocument doc) {
		final Action action = doc.getAction();

		try {
			if (action == Action.ADD || action == Action.UPDATE) {
				add(doc);
			} else if (action == Action.DELETE) {
				delete(doc);
			} else{
				failDocument(doc, new RequiredArgumentMissingException("Action not set in document. This document would never be stored or deleted."));
			}
		} catch (IOException e) {
			failDocument(doc, e);
		} catch (RequiredArgumentMissingException e) {
			failDocument(doc, e);
		}
	}

	@Override
	public void init() throws RequiredArgumentMissingException {
		outputFolder = new File(getOutputPath());
		if (!outputFolder.exists()){
			throw new RequiredArgumentMissingException("Output folder does not exist.");
		}
		if (!outputFolder.isDirectory()){
			throw new RequiredArgumentMissingException("Output folder name given is not a folder.");
		}
		if (!outputFolder.canWrite()){
			throw new RequiredArgumentMissingException("Not enought privileges to manipulate the output folder.");
		}
	}
	
	private void add(LocalDocument doc) throws IOException, RequiredArgumentMissingException {
		String jsonDocument = SerializationUtils.toJson(doc.getContentMap());
		String documentId = getDocumentId(doc);
		
		File outputFile = new File(outputFolder.getAbsolutePath() + File.separator + documentId + ".json");
		outputFile.createNewFile();
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF8"));
		out.append(jsonDocument);
		out.flush();
		out.close();
		
		accept(doc);
	}
	
	private void delete(LocalDocument doc) throws IOException, RequiredArgumentMissingException {
		String documentId = getDocumentId(doc);
		
		File outputFile = new File(getOutputPath() + File.separator + documentId + ".json");
		if (outputFile.exists()){
			outputFile.delete();
		}
		
		accept(doc);
	}
	
	private void failDocument(LocalDocument doc, Throwable reason) {
		try {
			Logger.error("Failing document " + doc.getID(), reason);
			fail(doc, reason);
		} catch (Exception e) {
			Logger.error("Could not fail document with hydra id: " + doc.getID(), e);
		}
	}
	
	private String getDocumentId (LocalDocument doc) throws RequiredArgumentMissingException {
		if(!doc.hasContentField(idField)) {
			throw new RequiredArgumentMissingException("Document has no ID field");
		}
		Object docId = doc.getContentField(idField);
		if(!(docId instanceof String)) {
			throw new RequiredArgumentMissingException("Document's ID field is not String");
		}
		return docId.toString();
	}

	/**
	 * Only used by the junit test
	 * 
	 * @param sendAll
	 */
	protected void setSendAll(boolean sendAll) {
		this.sendAll = sendAll;
	}
	
	public String getIdField () {
		return this.idField;
	}
	
	public void setIdField (String idField){
		this.idField = idField;
	}
	
	public String getOutputPath() {
		return this.outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	protected void setOutputFolder (File outputFolder) {
		this.outputFolder = outputFolder;
	}

}
