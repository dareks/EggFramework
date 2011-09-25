/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

import framework.Rule.Pattern;

/**
 * Routing class responsible for matching servlet paths to patterns. It is pretty fast - for 20 routes it takes 0,15ms on Intel i5 2500k
 * 
 * @author Jacek Olszak
 * 
 */
public final class Routing {

    private List<Rule> rules = Lists.newArrayList();
    private boolean closed;

    public Request route(String path, HttpServletRequest req) {
        for (Rule rule : rules) {
            Map<String, String> map = rule.getPattern().matches(path.toCharArray());
            if (map != null) {
                Pattern rewrite = rule.getRewritePattern();
                if (rewrite != null) {
                    String rewritePath = rewrite.toString(map);
                    Loggers.ROUTING.info("Match found {} => Rewriting to {}", rule.getPattern(), rewritePath);
                    return route(rewritePath, req);
                } else {
                    Loggers.ROUTING.info("Match found {}", rule.getPattern());
                    return new Request(req, map);
                }
            }
        }
        return null;
    }

    public boolean hasAnyRules() {
        return !rules.isEmpty();
    }

    public void addRule(Rule rule) {
        if (!closed) {
            this.rules.add(rule);
        } else {
            throw new RuntimeException("Routing rules can only be added in start method of services.Application class");
        }
    }

    public void close() {
        closed = true;
    }

}
