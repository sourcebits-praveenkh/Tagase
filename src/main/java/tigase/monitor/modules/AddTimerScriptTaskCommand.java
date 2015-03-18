package tigase.monitor.modules;

import tigase.component.adhoc.AdHocCommand;
import tigase.component.adhoc.AdHocCommandException;
import tigase.component.adhoc.AdHocResponse;
import tigase.component.adhoc.AdhHocRequest;
import tigase.form.Field;
import tigase.form.Form;
import tigase.kernel.Inject;
import tigase.kernel.Kernel;
import tigase.monitor.tasks.ScriptTimerTask;
import tigase.xml.Element;
import tigase.xmpp.Authorization;
import tigase.xmpp.JID;

public class AddTimerScriptTaskCommand implements AdHocCommand {

	public static final String ID = "x-add-timer-task";

	@Inject
	private Kernel kernel;

	@Override
	public void execute(AdhHocRequest request, AdHocResponse response) throws AdHocCommandException {
		try {
			final Element data = request.getCommand().getChild("x", "jabber:x:data");

			if (request.getAction() != null && "cancel".equals(request.getAction())) {
				response.cancelSession();
			} else if (data == null) {
				Form form = new Form("form", "Add monitor script", null);

				form.addField(Field.fieldTextSingle("scriptName", "", "Script name"));
				form.addField(Field.fieldTextSingle("delay", "1000", "Delay"));
				form.addField(Field.fieldTextSingle("scriptExtension", "", "Script extension"));
				form.addField(Field.fieldTextMulti("scriptContent", "", "Script"));

				response.getElements().add(form.getElement());
				response.startSession();
			} else {
				Form form = new Form(data);

				if ("submit".equals(form.getType())) {
					String scriptName = form.getAsString("scriptName");
					String scriptExtension = form.getAsString("scriptExtension");
					String scriptContent = form.getAsString("scriptContent");
					Long delay = form.getAsLong("delay");

					kernel.registerBeanClass(scriptName, ScriptTimerTask.class);
					ScriptTimerTask scriptTask = kernel.getInstance(scriptName);
					scriptTask.run(scriptContent, scriptExtension, delay);
				}

				response.completeSession();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new AdHocCommandException(Authorization.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	public Kernel getKernel() {
		return kernel;
	}

	@Override
	public String getName() {
		return "Add monitor timer task";
	}

	@Override
	public String getNode() {
		return ID;
	}

	@Override
	public boolean isAllowedFor(JID jid) {
		return true;
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

}
