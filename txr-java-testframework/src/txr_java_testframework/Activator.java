package txr_java_testframework;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	//The shared instance.
	private static Activator plugin;


	static BundleContext getContext() {
		return context;
	}

    /**
	 * The constructor.
	 */
	public Activator() {
		super();
		plugin = this;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static Image createImage(String name) {
		String finalName = name;
		if(!name.startsWith("icons/")){//$NON-NLS-1$
			finalName = "icons/"+finalName;//$NON-NLS-1$
		}
		try {
			URL installURL = Activator.context.getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, finalName);
			return ImageDescriptor.createFromURL(url).createImage();
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor().createImage();
		}
	}

}
