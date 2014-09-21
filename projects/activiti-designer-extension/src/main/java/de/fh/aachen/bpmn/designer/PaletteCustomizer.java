package de.fh.aachen.bpmn.designer;

import java.util.ArrayList;
import java.util.List;

import org.activiti.designer.integration.palette.AbstractDefaultPaletteCustomizer;
import org.activiti.designer.integration.palette.PaletteEntry;

/**
 * Customizes the palette for the REST Service Tasks.
 * Needed?
 * @author Torben Hensgens
 * @since 1.0.0
 * @version 1
 */
public class PaletteCustomizer extends AbstractDefaultPaletteCustomizer {

	public List<PaletteEntry> disablePaletteEntries() {
		List<PaletteEntry> result = new ArrayList<PaletteEntry>();
//		
//		//Disable the mail task
//		result.add(PaletteEntry.MAIL_TASK);
		return result;
	}
}