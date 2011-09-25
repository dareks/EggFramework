package framework;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Rule {

    private final Pattern pattern;
    private HttpMethod method;
    private Pattern rewritePattern;

    public Rule(String matchPattern) {
        this.pattern = new Pattern(matchPattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Rule rewrite(String pathPattern) {
        this.rewritePattern = new Pattern(pathPattern);
        return this;
    }

    public Pattern getRewritePattern() {
        return rewritePattern;
    }

    public Rule via(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

    class Pattern {

        final List<PatternPart> parts = Lists.newArrayList();
        final String pattern;

        Pattern(String pattern) {
            this.pattern = pattern;
            char[] chars = pattern.toCharArray();
            int state = 0;
            StringBuilder text = new StringBuilder();
            for (char c : chars) {
                if (c == '$') {
                    if (text.length() > 0) {
                        parts.add(new LiteralPart(text.toString().toCharArray()));
                    }
                    state = 1;
                    text = new StringBuilder();
                } else if (state == 1 && (c == '/' || c == ' ' || c == '.' || c == '-')) {
                    state = 0;
                    parts.add(new PlaceholderPart(text.toString()));
                    text = new StringBuilder().append(c);
                } else {
                    text.append(c);
                }
            }
            if (text.length() > 0) {
                if (state == 0) {
                    parts.add(new LiteralPart(text.toString().toCharArray()));
                } else if (state == 1) {
                    parts.add(new PlaceholderPart(text.toString()));
                }
            }

        }

        /**
         * @return Null if does not match, map of placeholders values otherwise
         */
        Map<String, String> matches(char[] path) {
            Map<String, String> values = Maps.newHashMap();
            int position = 0;
            for (int partIdx = 0; partIdx < parts.size(); partIdx++) {
                PatternPart part = parts.get(partIdx);
                if (position >= path.length) {
                    return null;
                } else if (part instanceof LiteralPart) {
                    LiteralPart literalPart = (LiteralPart) part;
                    if (path.length < position + literalPart.literal.length) {
                        return null;
                    }
                    for (int i = position; i < position + literalPart.literal.length; i++) {
                        if (path[i] != literalPart.literal[i - position]) {
                            return null;
                        }
                    }
                    position = position + literalPart.literal.length;
                } else if (part instanceof PlaceholderPart) {
                    PlaceholderPart placeholderPart = (PlaceholderPart) part;
                    StringBuilder builder = new StringBuilder();
                    for (int i = position; i < path.length; i++) {
                        char c = path[i];
                        if (c != '/' && c != ' ' && c != '.' && c != '-') {
                            builder.append(path[i]);
                        } else {
                            break;
                        }
                    }
                    position = position + builder.length();
                    values.put(placeholderPart.name, builder.toString());
                }
            }
            return position == path.length ? values : null;
        }

        @Override
        public String toString() {
            return pattern;
        }

        public String toString(Map<String, String> params) {
            StringBuilder builder = new StringBuilder();
            for (PatternPart part : parts) {
                if (part instanceof LiteralPart) {
                    builder.append(((LiteralPart) part).literal);
                } else if (part instanceof PlaceholderPart) {
                    builder.append(params.get(((PlaceholderPart) part).name));
                }
            }
            return builder.toString();
        }
    }

    interface PatternPart {
    }

    class LiteralPart implements PatternPart {
        final char[] literal;

        LiteralPart(char[] literal) {
            this.literal = literal;
        }

        @Override
        public String toString() {
            return new String(literal);
        }

    }

    class PlaceholderPart implements PatternPart {
        final String name;

        PlaceholderPart(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "$" + new String(name);
        }
    }

}
