package com.qiaopi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.qiaopi.constant.AiConstant;
import com.qiaopi.dto.ChatDTO;
import com.qiaopi.handler.Ai.pojo.AiData;
import com.qiaopi.handler.Ai.pojo.MyModelData;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.ChatService;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.utils.redis.RedisUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.qiaopi.constant.AiConstant.*;
import static com.qiaopi.handler.Ai.ChatSocketHandler.sendMessageToUser;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class ChatServiceImpl implements ChatService {

    @Value("${zhipu.api.key}")
    private String API_SECRET_KEY;

    private ClientV4 client;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisUtils redisUtils;

    @PostConstruct
    public void init() {
        this.client = new ClientV4.Builder(API_SECRET_KEY)
                .enableTokenCache()
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();
    }

    @Override
    public boolean testSseInvoke(String message) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(chatMessage);
        String requestId = "test-" + String.valueOf(System.currentTimeMillis());

        // 启用联网搜索功能
        List<ChatTool> tools = new ArrayList<>();


        ChatTool chatTool = new ChatTool();
        chatTool.setType("web_search");
        WebSearch webSearch = new WebSearch();
        webSearch.setEnable(Boolean.TRUE);
        webSearch.setSearch_result(Boolean.TRUE);
        webSearch.setSearch_query("侨批 侨缘信使");
        chatTool.setWeb_search(webSearch);
        tools.add(chatTool);
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode webSearchNode = mapper.getNodeFactory().objectNode();
//        webSearchNode.put("enable", true);
//        webSearchNode.put("search_result", true);
//
//        Map<String, JsonNode> chatToolMap = new HashMap<>();
//        chatToolMap.put("type", mapper.getNodeFactory().textNode("web_search"));
//        chatToolMap.put("web_search", webSearchNode);
//
//        ChatTool chatTool = new ChatTool(mapper.getNodeFactory(), chatToolMap);
//        tools.add(chatTool);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("glm-4-flash") // 这个模型不要钱
                .stream(Boolean.TRUE)
                .messages(messages)
                .requestId(requestId)
                .tools(tools)
                .build();
        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);
        if (sseModelApiResp.isSuccess()) {
            AtomicBoolean isFirst = new AtomicBoolean(true);
            List<Choice> choices = new ArrayList<>();

            Choice choice = new Choice();
            StringBuilder answer = new StringBuilder();
            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                    .doOnNext(accumulator -> {

                        if (isFirst.getAndSet(false)) {
                            System.out.print("Response: ");
                        }
                        if (accumulator.getDelta() != null && accumulator.getDelta().getTool_calls() != null) {
                            String jsonString = JSON.toJSONString(accumulator.getDelta().getTool_calls());
                            System.out.println("tool_calls: " + jsonString);
                        }
                        if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                            System.out.print(accumulator.getDelta().getContent());
                            sendMessageToUser(1L, accumulator.getDelta().getContent());
                        }
                        answer.append(accumulator.getDelta().getContent());
                        BeanUtil.copyProperties(accumulator.getChoice(), choice);
                    })
                    .doOnComplete(() -> {
                        sendMessageToUser(1L, "\n");
                        choice.getDelta().setContent(answer.toString());
                        System.out.println(choice.getMessage());
                        choices.add(choice);
                    })
                    .lastElement()
                    .blockingGet();

            System.out.println(JSONUtil.toJsonStr(chatMessageAccumulator));

            ModelData data = new ModelData();
            data.setChoices(choices);
            data.setUsage(chatMessageAccumulator.getUsage());
            data.setId(chatMessageAccumulator.getId());
            data.setCreated(chatMessageAccumulator.getCreated());
            data.setRequestId(chatCompletionRequest.getRequestId());
            sseModelApiResp.setFlowable(null);
            sseModelApiResp.setData(data);
        }
        System.out.println("model output:" + JSON.toJSONString(sseModelApiResp));
        return true;
    }

    public Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    // 亲自操刀的方法
    public boolean testSseInvokeByHttpAndPrompt(String message) {
        HttpClient client = HttpClient.newHttpClient();

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(chatMessage);

        List<com.qiaopi.handler.Ai.pojo.ChatTool> tools = new ArrayList<>();
        com.qiaopi.handler.Ai.pojo.ChatTool chatTool = new com.qiaopi.handler.Ai.pojo.ChatTool();
        chatTool.setType("web_search");
        com.qiaopi.handler.Ai.pojo.WebSearch webSearch = new com.qiaopi.handler.Ai.pojo.WebSearch();
        webSearch.setEnable(Boolean.TRUE);
        webSearch.setSearch_result(Boolean.TRUE);
        webSearch.setSearch_query("侨批 侨缘信使");

        webSearch.setSearch_prompt("\"\"\"\n" +
                "# 以下是来自互联网的信息：\n" +
                "{search_result}\n" +
                "\n" +
                "# 当前日期: 2024-XX-XX\n" +
                "\n" +
                "# 要求：\n" +
                "根据最新发布的信息回答用户问题，当回答引用了参考信息时，必须在句末使用对应的[ref_序号]来标明参考信息来源。\n" +
                "\n" +
                "\"\"\"");
        chatTool.setWeb_search(webSearch);
        tools.add(chatTool);

        com.qiaopi.handler.Ai.pojo.ChatCompletionRequest chatCompletionRequest = com.qiaopi.handler.Ai.pojo.ChatCompletionRequest.builder()
                .model("glm-4-flash")
                .stream(Boolean.TRUE)
                .messages(messages)
                .tools(tools)
                .build();

//        String jsonStr = JSONUtil.toJsonStr(chatCompletionRequest);
        String jsonStr = JSON.toJSONString(chatCompletionRequest);
        System.out.println(jsonStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://open.bigmodel.cn/api/paas/v4/chat/completions"))
//                .uri(URI.create("https://open.bigmodel.cn/api/paas/v4/tools"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", API_SECRET_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonStr)).build();

        MyModelData modelData = new MyModelData();
        StringBuffer sb = new StringBuffer();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenAccept(inputStream -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.replaceFirst("^data:\\s*", ""); // Remove "data: " prefix
                            System.out.println("Response: " + line);
                            if (line.startsWith("{")) {
                                MyModelData chatResponse = JSON.parseObject(line, MyModelData.class);
                                if (chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                                    sendMessageToUser(1L, chatResponse.getChoices().get(0).getDelta().getContent());
                                    BeanUtil.copyProperties(chatResponse, modelData);
                                    sb.append(chatResponse.getChoices().get(0).getDelta().getContent());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .join();

        modelData.getChoices().get(0).getDelta().setContent(sb.toString());
        System.out.println(JSONUtil.toJsonStr(modelData));
        return true;
    }

    @Override
    public void chat(ChatDTO chatDTO) {
        // 创建HTTP客户端
        HttpClient client = HttpClient.newHttpClient();
        Long userId = chatDTO.getUserId();
        String message = chatDTO.getMessage();
        // 从Redis中获取聊天消息列表
        String key = CHAT_USER + userId + CHAT_CHATTING;
        // 多次测试发现有断联情况，所以加上重试机制，redisUtils.getWithRetry(key)方法会重试3次
        List<ChatMessage> messages = JSON.parseArray(redisUtils.getWithRetry(key), ChatMessage.class);
        if (CollUtil.isEmpty(messages)) {
            // 如果消息列表为空，则从Redis中获取系统提示消息
            messages = new ArrayList<>(Objects.requireNonNull(JSON.parseArray(redisUtils.getWithRetry(CHAT_SYSTEM_PROMPT), ChatMessage.class)));
        }

        // 创建用户发送的聊天消息
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        messages.add(chatMessage); // 将用户消息添加到消息列表

        // 创建工具列表
        List<ChatTool> tools = new ArrayList<>();
        ChatTool chatTool = new ChatTool();
        if (chatDTO.getIsWebSearch()) {
            chatTool.setType("web_search"); // 设置工具类型为web_search
            WebSearch webSearch = new WebSearch();
            webSearch.setEnable(Boolean.TRUE); // 启用搜索
            webSearch.setSearch_result(Boolean.TRUE); // 启用搜索结果
            // webSearch.setSearch_query("侨批 侨缘信使"); // 设置搜索查询（注释掉）
            chatTool.setWeb_search(webSearch); // 设置WebSearch对象
            tools.add(chatTool); // 将工具添加到工具列表
        }
        // 构建ChatCompletionRequest对象
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .requestId("chat-" + String.valueOf(System.currentTimeMillis())) // 设置请求ID
                .model(StringUtils.isNotEmpty(chatDTO.getChatModel()) ? chatDTO.getChatModel() : "GLM-4-Flash") // 设置模型
                .stream(Boolean.TRUE) // 设置流式传输
                .messages(messages) // 设置消息列表
                .maxTokens(4095) // 设置最大token数
                .tools(tools) // 设置工具列表
                .build();

        // 将ChatCompletionRequest对象转换为JSON字符串
        String jsonStr = JSON.toJSONString(chatCompletionRequest);
        //System.out.println(jsonStr); // 打印请求JSON字符串

        // 设置最大重试次数
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        // 重试机制
        while (retryCount < maxRetries && !success) {
            String answer = ""; // 初始化答案
            try {
                // 构建HttpRequest对象
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://open.bigmodel.cn/api/paas/v4/chat/completions")) // 设置请求URL
                        .timeout(Duration.ofSeconds(30)) // 设置超时时间
                        .header("Authorization", API_SECRET_KEY) // 设置授权头
                        .header("Content-Type", "application/json") // 设置内容类型
                        .POST(HttpRequest.BodyPublishers.ofString(jsonStr)).build(); // 设置请求体

                // 创建MyModelData对象用于存储响应数据
                MyModelData modelData = new MyModelData();
                StringBuffer sb = new StringBuffer(); // 用于拼接响应内容
                AtomicBoolean end = new AtomicBoolean(false); // 标记响应是否结束
                // 发送异步请求并处理响应
                client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                        .thenApply(HttpResponse::body)
                        .thenAccept(inputStream -> {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    line = line.replaceFirst("^data:\\s*", ""); // 去除前缀"data: "
//                                    System.out.println("Response: " + line); // 打印响应内容
                                    if (line.startsWith("{")) {
                                        // 解析JSON响应
                                        MyModelData chatResponse = JSON.parseObject(line, MyModelData.class);
                                        if (chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                                            // 将响应内容发送给WebSocket客户端
                                            responseMessage(userId, chatResponse.getChoices().get(0).getDelta().getContent());

                                            BeanUtil.copyProperties(chatResponse, modelData); // 复制属性到modelData
                                            sb.append(chatResponse.getChoices().get(0).getDelta().getContent()); // 拼接响应内容
                                        }
                                    }
                                    if (CODE_DONE.equals(line.trim())) {
                                        end.set(true); // 标记响应结束
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                log.error("Error reading response", e); // 记录错误日志
                                // 发送错误消息给WebSocket客户端
                                responseError(userId, MessageUtils.message("chat.response.error"));
                            }
                        })
                        .join(); // 等待异步任务完成

                // 设置最终答案
                answer = sb.toString();
                modelData.getChoices().get(0).getDelta().setContent(sb.toString()); // 设置最终内容
                if (end.get()) {
                    success = true; // 设置成功标志
                    responseSuccess(userId, MessageUtils.message("chat.response.success"), AiData.getDoneMessage()); // 发送成功消息给WebSocket客户端
                }
            } catch (Exception e) {
                retryCount++; // 增加重试次数
                if (retryCount >= maxRetries) {
                    responseError(userId, MessageUtils.message("chat.response.timeout")); // 发送超时消息给WebSocket客户端
                    throw e; // 达到最大重试次数后抛出异常
                }
                log.warn("Retrying... ({}/{})", retryCount, maxRetries); // 记录重试日志
            }

            // 将助手回复添加到消息列表
            messages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), answer));
            // 将更新后的消息列表存储回Redis
            redisUtils.setWithRetry(key, JSON.toJSONString(messages));
        }
    }

    @Override
    public void storeChat(Long userId) {
        String string = redisUtils.getWithRetry(CHAT_USER + userId + CHAT_CHATTING);
        if (string != null) {
            redisUtils.setWithRetry(AiConstant.CHAT_USER + userId + ":" + System.currentTimeMillis(), string);
            stringRedisTemplate.delete(AiConstant.CHAT_USER + userId + AiConstant.CHAT_CHATTING);
        }
        responseSuccess(userId, MessageUtils.message("chat.store.success"), new AiData(TYPE_USER, CODE_CLEAN, null));
    }

    @Override
    public void getChatHistory(Long userId) {
        String key = AiConstant.CHAT_USER + userId + ":*";
        String string = "";
        List<ChatMessage> messages = new ArrayList<>();
        // 查最后一条消息
        Set<String> keys = stringRedisTemplate.keys(key);
        if (CollUtil.isNotEmpty(keys)) {
            keys.removeIf(k -> k.endsWith(AiConstant.CHAT_CHATTING));
        }
        while (CollUtil.isNotEmpty(keys)) {
            String latestKey = Collections.max(keys);
            string = redisUtils.getWithRetry(latestKey);
            messages = JSON.parseArray(string, ChatMessage.class);
            // 移除系统消息
            assert messages != null;
            messages.removeIf(chatMessage -> Objects.equals(chatMessage.getRole(), ChatMessageRole.SYSTEM.value()));
            if (messages.isEmpty()) {
                stringRedisTemplate.delete(latestKey);
                keys.remove(latestKey);
            } else {
                break;
            }
        }
        if(CollUtil.isNotEmpty(messages)){
            storeChat(userId);
            redisUtils.setWithRetry(CHAT_USER + userId + CHAT_CHATTING, string);
        }
        responseSuccess(userId, MessageUtils.message("chat.get.history.success"), new AiData(TYPE_USER, CODE_HISTORY, messages));
    }

    @Override
    public void getChattingHistory(Long userId) {
        String key = AiConstant.CHAT_USER + userId + AiConstant.CHAT_CHATTING;
        String string = redisUtils.getWithRetry(key);
        List<ChatMessage> messages = JSON.parseArray(string, ChatMessage.class);
        // 移除系统消息
        if(CollUtil.isNotEmpty(messages)) {
            messages.removeIf(chatMessage -> Objects.equals(chatMessage.getRole(), ChatMessageRole.SYSTEM.value()));
        }
        responseSuccess(userId, MessageUtils.message("chat.get.chatting.success"), new AiData(TYPE_SYSTEM, CODE_CHATTING, messages));
    }

    @Override
    public void help(Long currentUserId) {
        ChatMessage helpMe = JSON.parseObject(redisUtils.getWithRetry(CHAT_HELP_LIST), ChatMessage.class);
        responseSuccess(currentUserId, MessageUtils.message("chat.get.help"), new AiData(TYPE_USER, CODE_HELP, helpMe.getContent()));
    }

    @Override
    public void retry(Long userId) {
        List<ChatMessage> messages = JSON.parseArray(redisUtils.getWithRetry(CHAT_USER + userId + CHAT_CHATTING), ChatMessage.class);
        if (CollUtil.isNotEmpty(messages)&&messages.size()>2) {
            if(messages.get(messages.size() - 1).getRole().equals(ChatMessageRole.ASSISTANT.value())){
                messages.remove(messages.size() - 1);
                ChatMessage userMessage = messages.remove(messages.size() - 1);
                redisUtils.setWithRetry(CHAT_USER + userId + CHAT_CHATTING, JSON.toJSONString(messages));
                chat(new ChatDTO(userId, (String) userMessage.getContent()));
            }
        }
    }

    @Override
    public void sendInteractiveMessage(Long userId, String message, Object data) {
        responseSuccess(userId, message, new AiData(TYPE_INTERACTIVE, CODE_DONE, data));
    }

    private void responseMessage(Long userId, String message) {
        sendMessageToUser(userId, message);
    }

    private void responseError(Long userId, String message) {
        sendMessageToUser(userId, JSON.toJSONString(AjaxResult.error(message)));
    }

    private void responseSuccess(Long userId, String message, AiData aiData) {
        sendMessageToUser(userId, JSON.toJSONString(AjaxResult.success(message, aiData)));
    }
}