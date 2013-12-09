package example.javaex;

import com.innowhere.relproxy.RelProxyOnReloadListener;
import com.innowhere.relproxy.jproxy.JProxy;
import com.innowhere.relproxy.jproxy.JProxyConfig;
import com.innowhere.relproxy.jproxy.JProxyDiagnosticsListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import org.itsnat.core.http.ItsNatHttpServlet;
import org.itsnat.core.tmpl.ItsNatDocumentTemplate;
import org.itsnat.core.event.ItsNatServletRequestListener;

/**
 *
 * @author jmarranz
 */
public class JProxyExLoadApp
{
    public static void init(ItsNatHttpServlet itsNatServlet,ServletConfig config)
    {    
        ServletContext context = itsNatServlet.getItsNatServletContext().getServletContext();
        String pathInput = context.getRealPath("/") + "/WEB-INF/javaex/code/";           
        String classFolder = null; // context.getRealPath("/") + "/WEB-INF/classes";
        Iterable<String> compilationOptions = Arrays.asList(new String[]{"-source","1.6","-target","1.6"});
        long scanPeriod = 200;
        
        RelProxyOnReloadListener proxyListener = new RelProxyOnReloadListener() {
            public void onReload(Object objOld, Object objNew, Object proxy, Method method, Object[] args) {
                System.out.println("Reloaded " + objNew + " Calling method: " + method);
            }        
        };
        
        JProxyDiagnosticsListener diagnosticsListener = new JProxyDiagnosticsListener()
        {
            public void onDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics)
            {
                List<Diagnostic<? extends JavaFileObject>> diagList = diagnostics.getDiagnostics();                
                int i = 1;
                for (Diagnostic diag : diagList)
                {
                   System.err.println("Diagnostic " + i);
                   System.err.println("  code: " + diag.getCode());
                   System.err.println("  kind: " + diag.getKind());
                   System.err.println("  position: " + diag.getPosition());
                   System.err.println("  start position: " + diag.getStartPosition());
                   System.err.println("  end position: " + diag.getEndPosition());
                   System.err.println("  source: " + diag.getSource());
                   System.err.println("  message: " + diag.getMessage(null));
                   i++;
                }
            }
        };
        
        JProxyConfig jpConfig = JProxy.createJProxyConfig();
        jpConfig.setEnabled(true)
                .setRelProxyOnReloadListener(proxyListener)
                .setInputPath(pathInput)
                .setScanPeriod(scanPeriod)
                .setClassFolder(classFolder)
                .setCompilationOptions(compilationOptions)
                .setJProxyDiagnosticsListener(diagnosticsListener);
        
        JProxy.init(jpConfig);

        
        FalseDB db = new FalseDB();

        String pathPrefix = context.getRealPath("/") + "/WEB-INF/javaex/pages/";

        ItsNatDocumentTemplate docTemplate;
        docTemplate = itsNatServlet.registerItsNatDocumentTemplate("javaex","text/html", pathPrefix + "javaex.html");

        ItsNatServletRequestListener listener = JProxy.create(new example.javaex.JProxyExampleLoadListener(db), ItsNatServletRequestListener.class);
        docTemplate.addItsNatServletRequestListener(listener);
    } 
}




