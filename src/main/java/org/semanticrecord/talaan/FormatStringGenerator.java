/**
 * 
 */
package org.semanticrecord.talaan;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author Rex Sheridan
 *
 */
public interface FormatStringGenerator {
	
	String generateFormatString(String eventName, String code, List<Parameter> parametersList);

}
