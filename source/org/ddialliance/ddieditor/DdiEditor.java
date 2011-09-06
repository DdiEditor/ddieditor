package org.ddialliance.ddieditor;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddieditor.persistenceaccess.filesystem.FilesystemManager;

public class DdiEditor {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(".");
		File[] fileList = file.listFiles();
		String jarName = "org.ddialliance.ddieditor.jar";
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].getName().indexOf(jarName) > -1) {
				System.out.println("// ");
				System.out.println("// " + jarName + ":");
				try {
					JarFile jarFile = new JarFile(fileList[i]);
					Manifest manifest = jarFile.getManifest();
					java.util.Map<String, Attributes> entries = manifest
							.getEntries();
					for (Attributes values : entries.values()) {
						for (Entry<Object, Object> entry : values.entrySet()) {
							System.out.println("// " + entry.getKey() + ": "
									+ entry.getValue());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			} else {
				continue;
			}
		}
		System.out.println("// ");

		if (args.length > 0) {
			try {
				DdiEditor ddiEditor = new DdiEditor();
				ddiEditor.test(args[0]);
				System.out.println("// ");
				System.out.println("// Test successfull");
				System.out.println("// ");
				System.out.println("// Cleaning up after test");
				ddiEditor.clean();
				System.out.println("// ");
				System.out.println("// Clean successfull");
				System.out.println("// ");
			} catch (Exception e) {
				System.out.println("// ");
				System.out.println("// Exception");
				System.out.println("// ");
				e.printStackTrace();
				System.out.println("// ");
			}
		} else {
			System.out.println("// To test instalation:");
			System.out.println("//  - add path to DBXML lib");
			System.out.println("//  - add path of DDI file to test upon");
			System.out
					.println("//  - aka: java -Djava.library.path=$DBXML_HOME/lib -jar org.ddialliance.ddieditor.jar path-to-ddi-file");
			System.out.println("// ");
		}
	}

	public void test(String path) throws Exception {
		FilesystemManager.getInstance().addResource(new File(path));
	}

	public void clean() throws Exception {
		// dbxml
		File[] files = DbXmlManager.getInstance().getEnvHome().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains("__")) {
				files[i].delete();
			}
			if (files[i].getName().contains("log.")) {
				files[i].delete();
			}
			if (files[i].getName().contains("dbxml")) {
				files[i].delete();
			}
		}

		// logs
		files = new File("logs").listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains(".log")) {
				files[i].delete();
			}
		}
	}
}
