import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;


class CostomBucketAssigner extends DateTimeBucketAssigner {

    private JSONObject json;

    @Override
    public String getBucketId(Object element, Context context) {

        try {
            json = JSON.parseObject(element.toString());
        } catch (JSONException e) {
            return super.getBucketId(element,context);
        }


        System.out.println(element);
        return json.getString("date") + "/" + json.getString("event");
    }


}
