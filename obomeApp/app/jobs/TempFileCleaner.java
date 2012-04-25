package jobs;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import models.SessionViewModel;

import org.jfree.util.Log;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import edu.sabanciuniv.dataMining.experiment.models.Corpus;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

@OnApplicationStart
@Every(TempFileCleaner.FREQUENCY + "h")
public class TempFileCleaner extends Job<Object> {
	
	public static final int FILE_TIMEOUT=12;
	public static final int FREQUENCY=1;
	
	private void deleteFile(File file) {
		// Delete with error logging.
		try {
			file.delete();
		} catch(RuntimeException e) {
			Log.error("Could not delete file.", e);
		}
	}
	
	private void checkFile(File file, Date staleDate) {
		// If this file does not exist, then ignore (unlikely).
		if (!file.exists()) {
			return;
		}
		
		File[] files = file.listFiles();
		
		if (files != null && files.length > 0) {
			// If it's a non-empty folder, check all files.
			for (File tempFile : files) {
				this.checkFile(tempFile, staleDate);
			}
			
			// If the folder is empty at the end, then we can delete it.
			files = file.listFiles();
			if (files.length == 0) {
				this.deleteFile(file);
			}
		} else {
			// If it's an empty folder or a file, then check the date and delete if necessary.
			Date modifiedDate = new Date(file.lastModified());
			if (modifiedDate.before(staleDate)) {
				this.deleteFile(file);
			}
		}
	}
	
	@Override
	public void doJob() {
		// Delete old temp files.
		Logger.info("Began cleaning temporary files.");
		
		// The directories to look in.
		List<File> directories = Lists.newArrayList(
			new File(new File(Play.configuration.getProperty("play.tmp", "tmp")),
				Play.configuration.getProperty("obome.downloads", "downloads")),
			new File(new File(Play.configuration.getProperty("play.tmp", "tmp")),
					"uploads"));
		
		// We delete everything that was modified before this time.
		Date staleDate = new DateTime().minusHours(FILE_TIMEOUT).toDate();

		// Check all files in these directories.
		for (File directory : directories) {
			if (directory.exists() && directory.isDirectory()) {
				for (File file : directory.listFiles()) {
					this.checkFile(file, staleDate);
				}
			}
		}
		
		Logger.info("Finished cleaning temporary files.");
	}
}