package org.ddialliance.ddieditor.logic.identification;

import java.util.Random;

import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.Log4jLog;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.ext.Log4jLogger;
import com.fasterxml.uuid.impl.RandomBasedGenerator;

public class UuidGenerator implements IdentificationGenerator {
	private String delimiter = "-";
	private RandomBasedGenerator randomUuid = null;
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			UuidGenerator.class);

	public UuidGenerator() {
		Log4jLogger.connectToLog4j(((Log4jLog) log).getLogger());
		randomUuid = Generators.randomBasedGenerator(new Random());
	}

	@Override
	public String generateId(String prefix, String postfix)
			throws DDIFtpException {
		StringBuffer id = new StringBuffer();
		if (prefix != null && !prefix.equals("")) {
			id.append(prefix);
			id.append(delimiter);
		}
		id.append(generateId());

		if (postfix != null && !postfix.equals("")) {
			id.append(delimiter);
			id.append(postfix);
		}
		return id.toString();
	}

	private String generateId() throws DDIFtpException {
		return randomUuid.generate().toString();
	}

	@Override
	public String getUserId() throws DDIFtpException {
		return System.getProperty("user.name");
	}

	@Override
	public String getDelimiter() {
		return delimiter;
	}
}
