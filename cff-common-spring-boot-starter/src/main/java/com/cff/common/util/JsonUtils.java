package com.cff.common.util;

import com.cff.common.exception.JsonSerializeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class JsonUtils {

    private JsonUtils() {
    }

    // 创建ObjectMapper实例，用于JSON处理
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 在反序列化时忽略在json中存在但java对象不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 大小写脱敏
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        // 接受空字符串序列化成数组
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        // 接受空数组序列化成对象
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static <T> T fromJson(String json, Class<T> type) throws IOException {
        return objectMapper.readValue(json, type);
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param source 对象
     * @return JSON字符串
     */
    public static String toJson(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new JsonSerializeException(e.getMessage());
        }
    }

    /**
     * 使用 <a href="https://datatracker.ietf.org/doc/html/draft-ietf-appsawg-json-pointer-03#section-3">JSON Pointer (RFC 6901)</a>
     * 语法 查找Json节点<br/>
     *
     * <p><b>核心语法规则：</b>
     * <ul>
     * <li>路径以 {@code /} 开头表示根节点（如 {@code /user/name}）</li>
     * <li>对象属性直接通过键名访问（如 {@code /profile/email}）</li>
     * <li>数组元素通过索引（从 0 开始）访问（如 {@code /items/1}）</li>
     * <li>特殊字符需转义：{@code ~} 转 {@code ~0}，{@code /} 转 {@code ~1}（如键 {@code "a/b"} 的路径为 {@code /a~1b}）</li>
     * <li>空指针 {@code ""} 返回整个文档，{@code "/"} 访问根下空键的值</li>
     * </ul>
     *
     * @param json 字符串
     * @param path 索引路径
     * @return JsonNode
     * @see com.fasterxml.jackson.core.JsonPointer
     */
    public static JsonNode findNode(String json, String path) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return rootNode.at(path);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    /**
     * 查找Json数组节点
     *
     * @param json 字符串
     * @param path 索引路径
     * @return ArrayNode数组节点
     * @throws JsonSerializeException 如果指定路径不是数组节点或JSON解析失败
     * @see #findNode(String, String)
     * @see com.fasterxml.jackson.databind.node.ArrayNode
     */
    public static ArrayNode findArray(String json, String path) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }

        JsonNode targetNode = rootNode.at(path);
        if (targetNode.isArray()) {
            return (ArrayNode) targetNode;
        }
        throw new JsonSerializeException("Not an array node: " + path);
    }

    /**
     * 将JsonNode转换为Map
     *
     * @param node JsonNode对象
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> nodeToMap(JsonNode node) {
        try {
            return objectMapper.convertValue(node, Map.class);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    /**
     * Map 转对象
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * 对象转 Map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, Object> toMap(T object) {
        return objectMapper.convertValue(object, Map.class);
    }

    public static <T> Map<String, T> convertToMap(Map<String, ?> fromMap, Class<T> valueType) {
        try {
            return objectMapper.convertValue(fromMap,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valueType));
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> Map<String, T> convertToMap(String json, TypeReference<Map<String, T>> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> List<T> convertToList(List<?> fromList, Class<T> valueType) {
        try {
            return objectMapper.convertValue(fromList,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> List<T> convertToList(String json, TypeReference<List<T>> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static JsonNode findNode(JsonNode rootNode, String nodeName) {
        if (rootNode == null || StringUtils.isEmpty(nodeName)) {
            return null;
        }
        if (rootNode.has(nodeName)) {
            return rootNode.get(nodeName);
        }

        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode childNode = field.getValue();
            if (childNode.isObject() || childNode.isArray()) {
                JsonNode result = findNode(childNode, nodeName);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 查找Json结点
     *
     * @param json  JSON字符串
     * @param nodeName 节点名称
     * @return JsonNode
     */
    public static String findNodeValue(String json, String nodeName) throws JsonProcessingException {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(nodeName)) {
            return null;
        }
        JsonNode rootNode = objectMapper.readTree(json);
        rootNode = findNode(rootNode, nodeName);
        if (rootNode != null) {
            return rootNode.asText();
        }
        return null;
    }

    /**
     * 对象深拷贝（基于 JSON 序列化）
     */
    public static <T> T deepCopy(T object, Class<T> clazz) {
        if (object == null) return null;
        try {
            String json = toJson(object);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonSerializeException(e.getMessage());
        }
    }

    public static String merge(String originalJson, String nodeName, Object nodeObject) {
        try {
            JsonNode originalNode = objectMapper.readTree(originalJson);
            JsonNode nodeToSet = objectMapper.valueToTree(nodeObject);

            // 添加到 JSON 根节点
            ((com.fasterxml.jackson.databind.node.ObjectNode) originalNode).set(nodeName, nodeToSet);
            // 生成新 JSON 字符串
            return objectMapper.writeValueAsString(originalNode);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }
}
