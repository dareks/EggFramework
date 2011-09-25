package framework;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

import framework.Rule.Pattern;

/**
 * Routing class responsible for matching servlet paths to patterns. It is
 * pretty fast - for 20 routes it takes 0,15ms on Intel i5 2500k
 * 
 * @author Jacek Olszak
 * 
 */
public final class Routing {

    private List<Rule> rules = Lists.newArrayList();

    public Request route(String path, HttpServletRequest req) {
        for (Rule rule : rules) {
            Map<String, String> map = rule.getPattern().matches(path.toCharArray());
            if (map != null) {
                System.out.print("Match found " + rule.getPattern());
                Pattern rewrite = rule.getRewritePattern();
                if (rewrite != null) {
                    String rewritePath = rewrite.toString(map);
                    System.out.println(" => Rewriting to " + rewritePath);
                    return route(rewritePath, req);
                } else {
                    System.out.println();
                    return new Request(req, map);
                }
            }
        }
        return null;
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

}
