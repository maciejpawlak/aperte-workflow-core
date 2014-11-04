package pl.net.bluesoft.rnd.processtool.web.domain;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Łukasz Karwot (BlueSoft)
 * Date: 18.04.13
 * Time: 12:39
 */
public class DataPagingBean<T> extends GenericResultBean {

	Collection<T> aaData;

    int	recordsTotal;

    int	recordsFiltered;

    int iDisplayStart = 0;

    int	draw = 1;

    public DataPagingBean(Collection<T> dataList, int pageLimit, String echo)
    {
        this.aaData = dataList;
        this.recordsFiltered = pageLimit;
        this.recordsTotal = dataList.size();
        this.draw = 1;

    }

    public Collection<T> getAaData() {
        return aaData;
    }

    public void setAaData(Collection<T> aaData) {
        this.aaData = aaData;
    }

    public int getiDisplayStart() {
        return iDisplayStart;
    }

    public void setiDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
}
