package ch.arond.ant.arduino;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CompileTask extends Task {

	private final String LIB_OUT = "lib.a";

	private String gcc;
	private String gcc_flags;
	
	private String gxx;
	private String gxx_flags;
	
	private String ar;

	private String libPath;
	private String include_list ="";
	
	
	private String flags = "";
	
	private String sourceFileList = "*.cpp";
	
	private String targetPath = "./";
	private File targetObjectDir;
	private File libFile;
	
	
	public void setGcc(String gcc) {
		this.gcc = gcc;
	}
	public void setGcc_flags(String gcc_flags) {
		this.gcc_flags = gcc_flags;
	}
	public void setGxx(String gxx) {
	    this.gxx = gxx;
    }
	public void setGxx_flags(String gxx_flags) {
	    this.gxx_flags = gxx_flags;
    }
	public void setLibPath(String libPath) {
	    this.libPath = libPath;
    }
	public void setFlags(String flags) {
	    this.flags = flags;
    }
	public void setSourceFileList(String sourceFileList) {
	    this.sourceFileList = sourceFileList;
    }
	public void setTargetPath(String targetPath) {
	    this.targetPath = targetPath;
    }
	public void setAr(String ar) {
	    this.ar = ar;
    }
	
	
	

	// The method executing the task
	public void execute() throws BuildException {

		System.out.println("gcc exec     :" + gcc);
		System.out.println("gcc flags    :" + gcc_flags);
		System.out.println("libPath      :" + libPath);
		
		System.out.println("targetPath   :" + targetPath);
		
		targetObjectDir = new File(targetPath,"objectDir");
		if(!targetObjectDir.exists()){
			targetObjectDir.mkdirs();
		}
		
		
		libFile = new File(targetPath,LIB_OUT);
		if(libFile.exists()){
			boolean deleted = libFile.delete();
			if(!deleted){
				throw new BuildException("Unable to delet "+libFile.getName());
			}
		}

		/**
		 * Compile Libraries
		 */
		String[] libPathList = libPath.split(",");
		for (String lib : libPathList) {
			include_list+=" -I"+lib;
		}
		System.out.println("include_list :" + include_list);
		for (String lib : libPathList) {
			attLibDir(lib);
		}
		
		
		/**
		 * Compile source
		 */
		String  out_elf = targetPath + File.separator + "main.elf";
		executeAndDisplay( gxx + " " + flags + 
		                   " " + include_list +
		                   " -I.  "+
		                   sourceFileList+
		                   " -o " + out_elf  +
		                   " -L. "+ libFile.getAbsolutePath() +" -lm"
		                   );
	}

	private void attLibDir(String lib) {
		System.out.println("attLibDir    :" + lib);
		File libPathF = new File(lib);
		
		if(!libPathF.exists()){
			System.err.println(libPathF.getAbsolutePath()+" does not exists");
			return;
		}
		
		File[] listOfFile = libPathF.listFiles();

		
		if(listOfFile == null) return;
		
		for (File f : listOfFile) {
			File out = compileLib(f);
			addToLib(out);
		}

	}

	
	


	private void addToLib(File out) {
	    if(out == null) return;
	    executeAndDisplay( ar + " rcs " +libFile.getAbsolutePath() + 
	    		          " " + out.getAbsolutePath());
    }

	private File compileLib(File f) {
		if(f.getName().endsWith(".c")){
			String  out = targetObjectDir.getAbsolutePath() + File.separator + "" + f.getName().substring(0, f.getName().length()-2) + ".o";
			executeAndDisplay( gcc + " " + gcc_flags + 
					           " " + include_list +
					           " -o " + out +
					           " "+f.getAbsolutePath());
			return new File(out);
		}
		
		if(f.getName().endsWith(".cpp")){
			String  out = targetObjectDir.getAbsolutePath() + File.separator + "" + f.getName().substring(0, f.getName().length()-2) + ".o";
			executeAndDisplay( gxx + " " + gxx_flags + 
					           " " + include_list +
					           " -o " + out +
					           " "+f.getAbsolutePath());
			return new File(out);
		}
	    return null;
    }

	/**
	 * 
	 * @param shellcommand
	 */
	private void executeAndDisplay(String shellcommand) {

//		shellcommand = shellcommand.replace("\\","/") ;
		System.out.println(shellcommand);
		
		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Runtime rt = Runtime.getRuntime();
			// Process pr = rt.exec("cmd /c dir");
			Process pr = rt.exec(shellcommand);

			
			error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			String line = null;
			while ((line = error.readLine()) != null) {
				System.err.println(line);
			}
			
			input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			line = null;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			

			int exitVal = pr.waitFor();
			if (exitVal != 0) {
				throw new BuildException("Exited with error code " + exitVal);
			}

		} catch (Exception e) {
			System.err.println(e.toString());
			throw new BuildException(e.getMessage(), e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (Exception e) {
				}
			if (error != null)
				try {
					error.close();
				} catch (Exception e) {
				}
		}
	}

}
