//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.qiaopi.handler.Ai.pojo;

import com.zhipu.oapi.core.model.ClientRequest;
import com.zhipu.oapi.service.v4.CommonRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMeta;
import com.zhipu.oapi.service.v4.model.SensitiveWordCheckRequest;
import com.zhipu.oapi.service.v4.model.params.CodeGeexExtra;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCompletionRequest extends CommonRequest implements ClientRequest<Map<String, Object>> {
    private String model;
    private List<ChatMessage> messages;
    private Boolean doSample;
    private Boolean stream;
    private Float temperature;
    private Float topP;
    private Integer maxTokens;
    private List<String> stop;
    private SensitiveWordCheckRequest sensitiveWordCheck;
    private List<ChatTool> tools;
    private ChatMeta meta;
    private CodeGeexExtra extra;
    private Object toolChoice;
    private String invokeMethod;

    public Map<String, Object> getOptions() {
        Map<String, Object> paramsMap = new HashMap();
        paramsMap.put("request_id", this.getRequestId());
        paramsMap.put("user_id", this.getUserId());
        paramsMap.put("messages", this.getMessages());
        paramsMap.put("model", this.getModel());
        paramsMap.put("stream", this.getStream());
        paramsMap.put("tools", this.getTools());
        paramsMap.put("tool_choice", this.getToolChoice());
        paramsMap.put("temperature", this.getTemperature());
        paramsMap.put("top_p", this.getTopP());
        paramsMap.put("sensitive_word_check", this.getSensitiveWordCheck());
        paramsMap.put("do_sample", this.getDoSample());
        paramsMap.put("max_tokens", this.getMaxTokens());
        paramsMap.put("stop", this.getStop());
        paramsMap.put("meta", this.getMeta());
        paramsMap.put("extra", this.getExtra());
        if (this.getExtraJson() != null) {
            paramsMap.putAll(this.getExtraJson());
        }

        return paramsMap;
    }

    protected ChatCompletionRequest(ChatCompletionRequestBuilder<?, ?> b) {
        super(b);
        this.model = b.model;
        this.messages = b.messages;
        this.doSample = b.doSample;
        this.stream = b.stream;
        this.temperature = b.temperature;
        this.topP = b.topP;
        this.maxTokens = b.maxTokens;
        this.stop = b.stop;
        this.sensitiveWordCheck = b.sensitiveWordCheck;
        this.tools = b.tools;
        this.meta = b.meta;
        this.extra = b.extra;
        this.toolChoice = b.toolChoice;
        this.invokeMethod = b.invokeMethod;
    }

    public static ChatCompletionRequestBuilder<?, ?> builder() {
        return new ChatCompletionRequestBuilderImpl();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ChatCompletionRequest)) {
            return false;
        } else {
            ChatCompletionRequest other = (ChatCompletionRequest)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                Object this$doSample = this.getDoSample();
                Object other$doSample = other.getDoSample();
                if (this$doSample == null) {
                    if (other$doSample != null) {
                        return false;
                    }
                } else if (!this$doSample.equals(other$doSample)) {
                    return false;
                }

                Object this$stream = this.getStream();
                Object other$stream = other.getStream();
                if (this$stream == null) {
                    if (other$stream != null) {
                        return false;
                    }
                } else if (!this$stream.equals(other$stream)) {
                    return false;
                }

                label167: {
                    Object this$temperature = this.getTemperature();
                    Object other$temperature = other.getTemperature();
                    if (this$temperature == null) {
                        if (other$temperature == null) {
                            break label167;
                        }
                    } else if (this$temperature.equals(other$temperature)) {
                        break label167;
                    }

                    return false;
                }

                label160: {
                    Object this$topP = this.getTopP();
                    Object other$topP = other.getTopP();
                    if (this$topP == null) {
                        if (other$topP == null) {
                            break label160;
                        }
                    } else if (this$topP.equals(other$topP)) {
                        break label160;
                    }

                    return false;
                }

                Object this$maxTokens = this.getMaxTokens();
                Object other$maxTokens = other.getMaxTokens();
                if (this$maxTokens == null) {
                    if (other$maxTokens != null) {
                        return false;
                    }
                } else if (!this$maxTokens.equals(other$maxTokens)) {
                    return false;
                }

                Object this$model = this.getModel();
                Object other$model = other.getModel();
                if (this$model == null) {
                    if (other$model != null) {
                        return false;
                    }
                } else if (!this$model.equals(other$model)) {
                    return false;
                }

                label139: {
                    Object this$messages = this.getMessages();
                    Object other$messages = other.getMessages();
                    if (this$messages == null) {
                        if (other$messages == null) {
                            break label139;
                        }
                    } else if (this$messages.equals(other$messages)) {
                        break label139;
                    }

                    return false;
                }

                Object this$stop = this.getStop();
                Object other$stop = other.getStop();
                if (this$stop == null) {
                    if (other$stop != null) {
                        return false;
                    }
                } else if (!this$stop.equals(other$stop)) {
                    return false;
                }

                Object this$sensitiveWordCheck = this.getSensitiveWordCheck();
                Object other$sensitiveWordCheck = other.getSensitiveWordCheck();
                if (this$sensitiveWordCheck == null) {
                    if (other$sensitiveWordCheck != null) {
                        return false;
                    }
                } else if (!this$sensitiveWordCheck.equals(other$sensitiveWordCheck)) {
                    return false;
                }

                label118: {
                    Object this$tools = this.getTools();
                    Object other$tools = other.getTools();
                    if (this$tools == null) {
                        if (other$tools == null) {
                            break label118;
                        }
                    } else if (this$tools.equals(other$tools)) {
                        break label118;
                    }

                    return false;
                }

                label111: {
                    Object this$meta = this.getMeta();
                    Object other$meta = other.getMeta();
                    if (this$meta == null) {
                        if (other$meta == null) {
                            break label111;
                        }
                    } else if (this$meta.equals(other$meta)) {
                        break label111;
                    }

                    return false;
                }

                label104: {
                    Object this$extra = this.getExtra();
                    Object other$extra = other.getExtra();
                    if (this$extra == null) {
                        if (other$extra == null) {
                            break label104;
                        }
                    } else if (this$extra.equals(other$extra)) {
                        break label104;
                    }

                    return false;
                }

                Object this$toolChoice = this.getToolChoice();
                Object other$toolChoice = other.getToolChoice();
                if (this$toolChoice == null) {
                    if (other$toolChoice != null) {
                        return false;
                    }
                } else if (!this$toolChoice.equals(other$toolChoice)) {
                    return false;
                }

                Object this$invokeMethod = this.getInvokeMethod();
                Object other$invokeMethod = other.getInvokeMethod();
                if (this$invokeMethod == null) {
                    if (other$invokeMethod != null) {
                        return false;
                    }
                } else if (!this$invokeMethod.equals(other$invokeMethod)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ChatCompletionRequest;
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = super.hashCode();
        Object $doSample = this.getDoSample();
        result = result * 59 + ($doSample == null ? 43 : $doSample.hashCode());
        Object $stream = this.getStream();
        result = result * 59 + ($stream == null ? 43 : $stream.hashCode());
        Object $temperature = this.getTemperature();
        result = result * 59 + ($temperature == null ? 43 : $temperature.hashCode());
        Object $topP = this.getTopP();
        result = result * 59 + ($topP == null ? 43 : $topP.hashCode());
        Object $maxTokens = this.getMaxTokens();
        result = result * 59 + ($maxTokens == null ? 43 : $maxTokens.hashCode());
        Object $model = this.getModel();
        result = result * 59 + ($model == null ? 43 : $model.hashCode());
        Object $messages = this.getMessages();
        result = result * 59 + ($messages == null ? 43 : $messages.hashCode());
        Object $stop = this.getStop();
        result = result * 59 + ($stop == null ? 43 : $stop.hashCode());
        Object $sensitiveWordCheck = this.getSensitiveWordCheck();
        result = result * 59 + ($sensitiveWordCheck == null ? 43 : $sensitiveWordCheck.hashCode());
        Object $tools = this.getTools();
        result = result * 59 + ($tools == null ? 43 : $tools.hashCode());
        Object $meta = this.getMeta();
        result = result * 59 + ($meta == null ? 43 : $meta.hashCode());
        Object $extra = this.getExtra();
        result = result * 59 + ($extra == null ? 43 : $extra.hashCode());
        Object $toolChoice = this.getToolChoice();
        result = result * 59 + ($toolChoice == null ? 43 : $toolChoice.hashCode());
        Object $invokeMethod = this.getInvokeMethod();
        result = result * 59 + ($invokeMethod == null ? 43 : $invokeMethod.hashCode());
        return result;
    }

    public ChatCompletionRequest() {
    }

    public ChatCompletionRequest(String model, List<ChatMessage> messages, Boolean doSample, Boolean stream, Float temperature, Float topP, Integer maxTokens, List<String> stop, SensitiveWordCheckRequest sensitiveWordCheck, List<ChatTool> tools, ChatMeta meta, CodeGeexExtra extra, Object toolChoice, String invokeMethod) {
        this.model = model;
        this.messages = messages;
        this.doSample = doSample;
        this.stream = stream;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.stop = stop;
        this.sensitiveWordCheck = sensitiveWordCheck;
        this.tools = tools;
        this.meta = meta;
        this.extra = extra;
        this.toolChoice = toolChoice;
        this.invokeMethod = invokeMethod;
    }

    public String getModel() {
        return this.model;
    }

    public List<ChatMessage> getMessages() {
        return this.messages;
    }

    public Boolean getDoSample() {
        return this.doSample;
    }

    public Boolean getStream() {
        return this.stream;
    }

    public Float getTemperature() {
        return this.temperature;
    }

    public Float getTopP() {
        return this.topP;
    }

    public Integer getMaxTokens() {
        return this.maxTokens;
    }

    public List<String> getStop() {
        return this.stop;
    }

    public SensitiveWordCheckRequest getSensitiveWordCheck() {
        return this.sensitiveWordCheck;
    }

    public List<ChatTool> getTools() {
        return this.tools;
    }

    public ChatMeta getMeta() {
        return this.meta;
    }

    public CodeGeexExtra getExtra() {
        return this.extra;
    }

    public Object getToolChoice() {
        return this.toolChoice;
    }

    public String getInvokeMethod() {
        return this.invokeMethod;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setDoSample(Boolean doSample) {
        this.doSample = doSample;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public void setSensitiveWordCheck(SensitiveWordCheckRequest sensitiveWordCheck) {
        this.sensitiveWordCheck = sensitiveWordCheck;
    }

    public void setTools(List<ChatTool> tools) {
        this.tools = tools;
    }

    public void setMeta(ChatMeta meta) {
        this.meta = meta;
    }

    public void setExtra(CodeGeexExtra extra) {
        this.extra = extra;
    }

    public void setToolChoice(Object toolChoice) {
        this.toolChoice = toolChoice;
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public String toString() {
        return "ChatCompletionRequest(model=" + this.getModel() + ", messages=" + this.getMessages() + ", doSample=" + this.getDoSample() + ", stream=" + this.getStream() + ", temperature=" + this.getTemperature() + ", topP=" + this.getTopP() + ", maxTokens=" + this.getMaxTokens() + ", stop=" + this.getStop() + ", sensitiveWordCheck=" + this.getSensitiveWordCheck() + ", tools=" + this.getTools() + ", meta=" + this.getMeta() + ", extra=" + this.getExtra() + ", toolChoice=" + this.getToolChoice() + ", invokeMethod=" + this.getInvokeMethod() + ")";
    }

    private static final class ChatCompletionRequestBuilderImpl extends ChatCompletionRequestBuilder<ChatCompletionRequest, ChatCompletionRequestBuilderImpl> {
        private ChatCompletionRequestBuilderImpl() {
        }

        protected ChatCompletionRequestBuilderImpl self() {
            return this;
        }

        public ChatCompletionRequest build() {
            return new ChatCompletionRequest(this);
        }
    }

    public abstract static class ChatCompletionRequestBuilder<C extends ChatCompletionRequest, B extends ChatCompletionRequestBuilder<C, B>> extends CommonRequest.CommonRequestBuilder<C, B> {
        private String model;
        private List<ChatMessage> messages;
        private Boolean doSample;
        private Boolean stream;
        private Float temperature;
        private Float topP;
        private Integer maxTokens;
        private List<String> stop;
        private SensitiveWordCheckRequest sensitiveWordCheck;
        private List<ChatTool> tools;
        private ChatMeta meta;
        private CodeGeexExtra extra;
        private Object toolChoice;
        private String invokeMethod;

        public ChatCompletionRequestBuilder() {
        }

        public B model(String model) {
            this.model = model;
            return this.self();
        }

        public B messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this.self();
        }

        public B doSample(Boolean doSample) {
            this.doSample = doSample;
            return this.self();
        }

        public B stream(Boolean stream) {
            this.stream = stream;
            return this.self();
        }

        public B temperature(Float temperature) {
            this.temperature = temperature;
            return this.self();
        }

        public B topP(Float topP) {
            this.topP = topP;
            return this.self();
        }

        public B maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this.self();
        }

        public B stop(List<String> stop) {
            this.stop = stop;
            return this.self();
        }

        public B sensitiveWordCheck(SensitiveWordCheckRequest sensitiveWordCheck) {
            this.sensitiveWordCheck = sensitiveWordCheck;
            return this.self();
        }

        public B tools(List<ChatTool> tools) {
            this.tools = tools;
            return this.self();
        }

        public B meta(ChatMeta meta) {
            this.meta = meta;
            return this.self();
        }

        public B extra(CodeGeexExtra extra) {
            this.extra = extra;
            return this.self();
        }

        public B toolChoice(Object toolChoice) {
            this.toolChoice = toolChoice;
            return this.self();
        }

        public B invokeMethod(String invokeMethod) {
            this.invokeMethod = invokeMethod;
            return this.self();
        }

        protected abstract B self();

        public abstract C build();

        public String toString() {
            return "ChatCompletionRequest.ChatCompletionRequestBuilder(super=" + super.toString() + ", model=" + this.model + ", messages=" + this.messages + ", doSample=" + this.doSample + ", stream=" + this.stream + ", temperature=" + this.temperature + ", topP=" + this.topP + ", maxTokens=" + this.maxTokens + ", stop=" + this.stop + ", sensitiveWordCheck=" + this.sensitiveWordCheck + ", tools=" + this.tools + ", meta=" + this.meta + ", extra=" + this.extra + ", toolChoice=" + this.toolChoice + ", invokeMethod=" + this.invokeMethod + ")";
        }
    }
}
