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
    public String addView() {
        return noMap("src/main/resources/private/freemarker/add.ftl");
    }

    public String login() {
        return noMap("src/main/resources/private/freemarker/login.ftl");
    }

    public String register() {
        return noMap("src/main/resources/private/freemarker/register.ftl");
    }

    public String detailView(Judge j) {
        return judgeOnly(j, "src/main/resources/private/freemarker/detail.ftl");
    }

    public String editView(Judge j) {
        return judgeOnly(j, "src/main/resources/private/freemarker/edit.ftl");
    }

    public String judgeList(List<Judge> judges) {
        Configuration cfg = new Configuration();
        Map<String, Object> root = new HashMap<>();
        root.put("judges", judges);
        Writer out = new StringWriter();
        freemarker.template.Template temp = null;
        try {
            temp = cfg.getTemplate("src/main/resources/private/freemarker/main.ftl");
            temp.process(root, out);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        return out.toString();
    }

    private String judgeOnly(Judge j, String templateLocation) {
        Configuration cfg = new Configuration();
        Map<String, Object> root = new HashMap<>();
        root.put("judge", j);
        Writer out = new StringWriter();
        freemarker.template.Template temp = null;
        try {
            temp = cfg.getTemplate(templateLocation);
            temp.process(root, out);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        return out.toString();
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
