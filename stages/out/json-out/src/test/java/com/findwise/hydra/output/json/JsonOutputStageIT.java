package com.findwise.hydra.output.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.findwise.hydra.common.Document.Action;
import com.findwise.hydra.local.LocalDocument;
import com.findwise.hydra.local.RemotePipeline;
import com.findwise.hydra.output.json.JsonOutputStage;

public class JsonOutputStageIT {

	JsonOutputStage jsonOutput;
	String outFolderPath;
	File outFolder;
	RemotePipeline mockRP;
	LocalDocument addDocument;
	LocalDocument deleteDocument;

	@Before
	public void setUp() throws Exception {
		jsonOutput = new JsonOutputStage();
		mockRP = Mockito.mock(RemotePipeline.class);
		
		outFolderPath = System.getProperty("user.dir") + "/jsonOutputStageTest";
		outFolder = new File(outFolderPath);
		outFolder.mkdir();

		jsonOutput.setOutputPath(outFolderPath);
		jsonOutput.setOutputFolder(outFolder);
		jsonOutput.setRemotePipeline(mockRP);
		jsonOutput.setIdField("id");
				
		addDocument = new LocalDocument();
		addDocument.setAction(Action.ADD);
		addDocument.putContentField(jsonOutput.getIdField(), "document1");
		addDocument.putContentField("field", "value");
		
		deleteDocument = new LocalDocument();
		deleteDocument.setAction(Action.DELETE);
		deleteDocument.putContentField(jsonOutput.getIdField(), "document1");
		deleteDocument.putContentField("field", "value");
	}

	@After
	public void tearDown() throws Exception {
		File[] files = outFolder.listFiles();
		for (File file : files) {
			file.delete();
		}
		outFolder.delete();
	}
	
	@Test
	public void testAdd() throws Exception {
		LocalDocument doc = new LocalDocument();
		doc.setAction(Action.ADD);
		doc.putContentField("name", "jonas");
		doc.putContentField("id", "testAdd");
		jsonOutput.output(doc);
		assertTrue("The file has not been created", (new File(outFolderPath + '/' + "testAdd")).exists());
	}

	@Test
	public void testDelete() throws Exception {
		LocalDocument doc = new LocalDocument();
		doc.setAction(Action.ADD);
		doc.putContentField("name", "jonas");
		doc.putContentField("id", "testDel");
		jsonOutput.output(doc);
		assertTrue("The file has not been created", (new File(outFolderPath + '/' + "testDel")).exists());
		
		doc.setAction(Action.DELETE);
		doc.removeContentField("id");
		jsonOutput.output(doc);
		assertTrue("The file should not be deleted", (new File(outFolderPath + '/' + "testDel")).exists());
		
		doc.putContentField("id", "testDel");
		jsonOutput.output(doc);
		assertFalse("The file has not been created", (new File(outFolderPath + '/' + "testDel")).exists());
	}
	
	@Test
	public void checkContents() throws Exception {
		LocalDocument doc = new LocalDocument();
		doc.setAction(Action.ADD);
		doc.putContentField("name", "jonas");
		doc.putContentField("id", "testAdd");
		jsonOutput.output(doc);
		File f = new File(outFolderPath + '/' + "testAdd");
		assertTrue("The file has not been created", f.exists());
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8")); 
		String str = new String(), tmp;
		while ((tmp = in.readLine()) != null) {
			str += tmp;
		}
		assertEquals("The document contents do not match", doc.toJson(), str);
		in.close();
		
	}

}
