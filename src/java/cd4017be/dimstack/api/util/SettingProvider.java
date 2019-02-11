package cd4017be.dimstack.api.util;

import java.util.Collection;
import java.util.HashMap;

import cd4017be.dimstack.api.IDimensionSettings;

/**
 * Template class that implements providing dimension settings
 * @author CD4017BE
 */
public class SettingProvider {

	/** dimension settings mapped by type */
	private HashMap<Class<?extends IDimensionSettings>, IDimensionSettings> settings = new HashMap<>();

	/**
	 * @param type setting type
	 * @param create whether to create a new settings instance if absent
	 * @return the settings of given type
	 */
	@SuppressWarnings("unchecked")
	public <T extends IDimensionSettings> T getSettings(Class<T> type, boolean create) {
		return (T) (create ? settings.computeIfAbsent(type, IDimensionSettings::newInstance) : settings.get(type));
	}

	/**
	 * @return a list of all registered settings
	 */
	public Collection<IDimensionSettings> getAllSettings() {
		return settings.values();
	}

}
