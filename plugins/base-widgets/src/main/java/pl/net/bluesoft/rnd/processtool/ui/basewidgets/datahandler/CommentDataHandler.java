package pl.net.bluesoft.rnd.processtool.ui.basewidgets.datahandler;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Maciej
 */
public class CommentDataHandler implements IWidgetDataHandler {
    private static final String TYPE_COMMENT = "comment";

    @Override
	public void handleWidgetData(IAttributesConsumer consumer, WidgetData data) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ProcessCommentBean.class);

        for (WidgetDataEntry commentData : data.getEntriesByType(TYPE_COMMENT)) {
            String commentsJSON = commentData.getValue();
            try {
                List<ProcessCommentBean> list = mapper.readValue(commentsJSON, type);
                if (consumer.getProcessInstance() != null) {
                    List<ProcessComment> comments = convert(list, consumer);
                    consumer.getProcessInstance().addComments(comments);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<ProcessComment> convert(List<ProcessCommentBean> list, IAttributesProvider provider) {
        List<ProcessComment> result = new ArrayList<ProcessComment>();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        for (ProcessCommentBean bean : list) {
            result.add(convert(bean, provider, format));
        }
        return result;
    }

    private ProcessComment convert(ProcessCommentBean bean, IAttributesProvider provider, SimpleDateFormat format) {
        ProcessComment comment = new ProcessComment();
        comment.setAuthorLogin(bean.getAuthorLogin());
        comment.setAuthorFullName(bean.getAuthorFullName());
        comment.setBody(bean.getBody());
        if(provider instanceof BpmTask)
            comment.setProcessState(((BpmTask) provider).getTaskName());
        comment.setProcessInstance(provider.getProcessInstance());
        try {
            comment.setCreateTime(format.parse(bean.getCreateDate()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return comment;
    }
}
