package ee.ttu.idk0071.sentiment.utils;

import org.springframework.util.StringUtils;

public class PropertyNameHyphenizer implements PropertyNameModifier {

	@Override
	public String apply(String propertyName) {
		if (!StringUtils.isEmpty(propertyName)) {
			if (propertyName.startsWith("get"))
				propertyName = propertyName.replaceFirst("get", "");
			
			return propertyName.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
		}
		
		return propertyName;
	}

}
