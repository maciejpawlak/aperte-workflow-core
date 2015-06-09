package pl.net.bluesoft.rnd.processtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.ExpiringCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * Aperte settings provider
 *
 * @author: mpawlak@bluesoft.net.pl
 */
@Component
public class AperteSettingsProvider implements ISettingsProvider
{
    private final ExpiringCache<String, String> settings = new ExpiringCache<String, String>(60 * 1000);

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    @Override
    public String getSetting(IProcessToolSettings settingKey) {
        return getSetting(settingKey.toString());
    }

    @Override
    public String getSetting(final String key)
    {
        return settings.get(key, new ExpiringCache.NewValueCallback<String, String>() {
            @Override
            public String getNewValue(final String setting)
            {
                Connection connection = null;
                try {
                    connection = processToolRegistry.getDataRegistry().getDataSourceProxy().getConnection();

                    PreparedStatement preparedStatement =
                                connection.prepareStatement("select value_ from pt_setting where key_ = '"+key+"'");

                        ResultSet resultSet = preparedStatement.executeQuery();
                        if(resultSet.next())
                            return resultSet.getString(1);
                        else
                            return null;


                    }
                    catch (Throwable ex)
                    {
                        throw new RuntimeException("[SETTINGS] Error", ex);
                    }
                    finally {
                        try {
                            if(!connection.isClosed())
                            connection.close();
                    }
                    catch(Throwable ex)
                    {

                    }
                }


            }
        });
    }

    @Override
    public void setSetting(final IProcessToolSettings settingKey, final String value)
    {
        settings.put(settingKey.toString(), value);

        Connection connection = null;
        try {
            connection = processToolRegistry.getDataRegistry().getDataSourceProxy().getConnection();

            PreparedStatement preparedStatement =
                    connection.prepareStatement("update pt_setting set value_= '"+value+"' where key_ = '"+settingKey.toString()+"'");

            preparedStatement.executeUpdate();
        }
        catch (Throwable ex)
        {
            throw new RuntimeException("[SETTINGS] Error", ex);
        }
        finally {
            try {
                if(!connection.isClosed())
                    connection.close();
            }
            catch(Throwable ex)
            {

            }
        }
    }

    @Override
    public void invalidateCache() {
        settings.clear();
    }

}
