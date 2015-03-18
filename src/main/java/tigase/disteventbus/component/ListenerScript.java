package tigase.disteventbus.component;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import tigase.disteventbus.EventBus;
import tigase.disteventbus.EventHandler;
import tigase.kernel.Kernel;
import tigase.xml.Element;

public class ListenerScript implements EventHandler {

	private CompiledScript compiledScript;

	private ScriptEngine engine;

	private String eventName;

	private String eventXMLNS;

	private Kernel kernel;

	private String scriptContent;

	@Override
	public void onEvent(String name, String xmlns, Element event) {
		try {
			Bindings bindings = engine.createBindings();
			bindings.put("event", event);
			bindings.put("eventName", name);
			bindings.put("eventXMLNS", xmlns);

			if (this.compiledScript != null) {
				this.compiledScript.eval(bindings);
			} else {
				this.engine.eval(scriptContent, bindings);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run(Kernel kernel, ScriptEngineManager scriptEngineManager, String scriptName, String scriptExtension,
			String scriptContent, String eventName, String eventXMLNS) throws ScriptException {
		this.kernel = kernel;
		this.eventName = eventName;
		this.eventXMLNS = eventXMLNS;
		this.engine = scriptEngineManager.getEngineByExtension(scriptExtension);
		this.scriptContent = scriptContent;
		if (engine instanceof Compilable) {
			this.compiledScript = ((Compilable) engine).compile(scriptContent);
		} else {
			this.compiledScript = null;
		}

		((EventBus) kernel.getInstance("eventBus")).addHandler(this.eventName, this.eventXMLNS, this);
	}

	public void unregister() {
		((EventBus) kernel.getInstance("eventBus")).removeHandler(this.eventName, this.eventXMLNS, this);
	}
}
