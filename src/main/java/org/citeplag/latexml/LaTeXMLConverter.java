package org.citeplag.latexml;

import org.apache.log4j.Logger;
import org.citeplag.util.CommandExecutor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Main class for conversion from a latex formula to
 * a MathML representation.
 *
 * @author Vincent Stange
 */
public class LaTeXMLConverter {

    private static Logger logger = Logger.getLogger(LaTeXMLConverter.class);

    private final LateXMLConfig lateXMLConfig;

    public LaTeXMLConverter(LateXMLConfig lateXMLConfig) {
        this.lateXMLConfig = lateXMLConfig;
    }

    /**
     * This methods needs a LaTeXML installation. It converts a latex formula
     * string into mathml and includes pmml, cmml and tex semantics.
     * Conversion is executed by "latexmlc".
     *
     * @param latex Latex Formula
     * @return MathML representation as String
     * @throws Exception Execution of latexmlc failed.
     */
    public String runLatexmlc(String latex) throws Exception {
        CommandExecutor latexmlmath = new CommandExecutor("latexmlc",
                "--includestyles",
                "--format=xhtml",
                "--whatsin=math",
                "--whatsout=math",
                "--pmml",
                "--cmml",
                "--nodefaultresources",
                "--linelength=90",
                "--quiet",
                "--preload", "LaTeX.pool",
                "--preload", "article.cls",
                "--preload", "amsmath.sty",
                "--preload", "amsthm.sty",
                "--preload", "amstext.sty",
                "--preload", "amssymb.sty",
                "--preload", "eucal.sty",
                "--preload", "[dvipsnames]xcolor.sty",
                "--preload", "url.sty",
                "--preload", "hyperref.sty",
                "--preload", "[ids]latexml.sty",
                "--preload", "texvc",
                "literal:" + latex);
        return latexmlmath.exec(2000L);
    }

    public String convertLatexmlService(String latex) {
        String payload = "format=xhtml" +
                configToUrlString(lateXMLConfig.getParams()) +
                "&tex=literal:"
                + latex;

        RestTemplate restTemplate = new RestTemplate();
        try {
            ServiceResponse rep = restTemplate.postForObject(lateXMLConfig.getUrl(), payload, ServiceResponse.class);
            logger.info(String.format("statusCode: %s\nstatus: %s\nlog: %s\nresult: %s", rep.getStatusCode(), rep.getStatus(), rep.getLog(), rep.getResult()));
            return rep.getResult();
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw e;
        }
    }

    private String configToUrlString(Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        values.forEach((k, v) -> {
            // value splitting or create a array with a single or null string
            String[] list = v.contains(",") ? v.split(",") : new String[]{v};
            for (String value : list) {
                sb.append("&").append(k);
                if (!"".equals(value)) {
                    sb.append("=").append(v);
                }
            }
        });
        return sb.toString();
    }

}