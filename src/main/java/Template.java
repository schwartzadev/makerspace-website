import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew Schwartz on 11/23/17.
 */
public class Template {
    public String login() {
        return noMap("src/main/resources/private/freemarker/login.ftl");
    }

    public String register() {
        return noMap("src/main/resources/private/freemarker/register.ftl");
    }

    private String noMap(String templateLocation) {
        Configuration cfg = new Configuration();
        Writer out = new StringWriter();
        freemarker.template.Template temp = null;
        try {
            temp = cfg.getTemplate(templateLocation);
            temp.process(new HashMap<>(), out);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        return out.toString();
    }
}
