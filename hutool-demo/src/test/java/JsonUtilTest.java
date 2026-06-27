import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


public class JsonUtilTest {


    String json = "{file_guid: \"31a37cbdf8e37f85026f046a7580d2c0\", filename: \"Java并发面试\", source: 1}";

    @BeforeAll
    static void beforeAll() {

    }

    @Test
    void JsonUtilTest() {

        Map params = JSONUtil.toBean(json, Map.class);
        // HashMap bean = JSONUtil.parseObj(json).toBean(HashMap.class);

        params.forEach((k, v) -> System.out.println(k + ":" + v));

    }
}
