package org.ddialliance.ddieditor.persistenceaccess;

import java.util.ArrayList;
import java.util.List;

import org.ddialliance.ddieditor.model.XQuery;
import org.ddialliance.ddiftp.util.DDIFtpException;

public class ParamatizedXquery {
	private String[] parameters = null;
	private String[] xQuery = null;

	public ParamatizedXquery(XQuery xQuery) {
		paramatizeXquery(xQuery.getFullQueryString());
	}

	public ParamatizedXquery(String query) {
		paramatizeXquery(query);
	}

	private void paramatizeXquery(String query) {
		List<String> xQueryList = new ArrayList<String>();
		boolean inQuotes = false;
		int lastParmEnd = 0;

		for (int i = 0; i < query.length(); ++i) {
			int _char = query.charAt(i);

			if (_char == '\'')
				inQuotes = !inQuotes;
			if (_char == '?' && !inQuotes) {
				xQueryList.add(query.substring(lastParmEnd, i));
				lastParmEnd = i + 1;
			}
		}
		xQueryList.add(query.substring(lastParmEnd, query.length()));
		parameters = new String[xQueryList.size() - 1];
		xQuery = new String[xQueryList.size()];
		for (int j = 0; j < xQuery.length; j++) {
			xQuery[j] = xQueryList.get(j);
		}
		clearParameters();
	}

	public String getParamatizedQuery() throws DDIFtpException {
		StringBuilder result = new StringBuilder();
		int i = 0;
		for (i = 0; i < parameters.length; ++i) {
			if (parameters[i] == null) {
				throw new DDIFtpException("No value specified for parameter "
						+ (i + 1) + " in query: " + toString());
			} else {
				result.append(xQuery[i]);
				result.append(parameters[i]);
			}
		}
		result.append(xQuery[i]);
		return result.toString();
	}

	public void clearParameters() {
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = null;
		}
	}

	private final void set(int parameterIndex, String parameter)
			throws DDIFtpException {
		if (parameterIndex < 1 || parameterIndex - 1 > parameters.length) {
			throw new DDIFtpException(
					"Parameter index: "
							+ parameterIndex
							+ " is out of range. Ranged defined as: 0 > parameter index < "
							+ parameters.length);
		}
		parameters[parameterIndex - 1] = parameter;
	}

	public void setObject(int parameterIndex, Object parameter)
			throws DDIFtpException {
		if (parameter instanceof String) {
			set(parameterIndex, parameter.toString());
		}
	}

	public void setString(int parameterIndex, String parameter)
			throws DDIFtpException {
		// if the passed string is null, then set this column to null

		if (parameter == null) {
			set(parameterIndex, "null");
		} else {
			StringBuilder result = new StringBuilder();
			int i;

			result.append('\'');

			for (i = 0; i < parameter.length(); ++i) {
				char c = parameter.charAt(i);

				if (c == '\\' || c == '\'' || c == '"') {
					result.append((char) '\\');
				}
				result.append(c);
			}

			result.append('\'');
			set(parameterIndex, result.toString());
		}
	}

	public int getParameterSize() {
		return parameters.length;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0, j = 1; i < xQuery.length; i++, j++) {
			result.append(xQuery[i]);
			if (j <= parameters.length) {
				result.append("[? param");
				result.append(j);
				result.append("]");	
			}
		}
		return result.toString();
	}
}
