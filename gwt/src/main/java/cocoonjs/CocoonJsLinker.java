package cocoonjs;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;

/**
 * Linker for CocoonJs
 */
@LinkerOrder(Order.PRIMARY)
public class CocoonJsLinker extends SelectionScriptLinker {

	@Override
	public String getDescription() {
		return "CocoonJs Linker";
	}

	@Override
	protected String getCompilationExtension(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		return ".cache.js";
	}

	@Override
	protected String getModulePrefix(TreeLogger logger, LinkerContext context, String strongName) throws UnableToCompleteException {
		return "";
	}

	@Override
	protected String getModuleSuffix2(TreeLogger logger, LinkerContext context, String strongName) throws UnableToCompleteException {
		return "";
	}

	@Override
	protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context) throws UnableToCompleteException {
		return "cocoonjs/CocoonJsTemplate.js";
	}
}
