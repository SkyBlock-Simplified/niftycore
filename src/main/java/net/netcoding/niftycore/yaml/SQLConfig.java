package net.netcoding.niftycore.yaml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import net.netcoding.niftycore.database.MySQL;
import net.netcoding.niftycore.database.PostgreSQL;
import net.netcoding.niftycore.database.SQLServer;
import net.netcoding.niftycore.database.factory.SQLWrapper;
import net.netcoding.niftycore.reflection.Reflection;
import net.netcoding.niftycore.yaml.annotations.Comment;
import net.netcoding.niftycore.yaml.annotations.Path;
import net.netcoding.niftycore.yaml.exceptions.InvalidConfigurationException;

public class SQLConfig<T extends SQLWrapper> extends Config {

	private transient SQLWrapper factory;

	@Comment("Database Driver (mysql, postgresql or sqlserver)")
	@Path("sql.driver")
	protected String driver = "sql";

	@Comment("Database Host")
	@Path("sql.host")
	protected String hostname = "localhost";

	@Comment("Database Username")
	@Path("sql.user")
	protected String username = "minecraft";

	@Comment("Database Password")
	@Path("sql.pass")
	protected String password = "";

	@Comment("Database Port")
	@Path("sql.port")
	protected int port = 0;

	@Comment("Database Name")
	@Path("sql.schema")
	protected String schema = "";

	public SQLConfig(File folder, String fileName, String... header) {
		this(folder, fileName, false, header);
	}

	public SQLConfig(File folder, String fileName, boolean skipFailedConversion, String... header) {
		super(folder, fileName);
	}

	public final String getHost() {
		return this.hostname;
	}

	public final String getUser() {
		return this.username;
	}

	public final String getPass() {
		return this.password;
	}

	public final int getPort() {
		return this.port;
	}

	public final String getSchema() {
		return this.schema;
	}

	public final SQLWrapper getSQL() {
		return this.getSuperClass().cast(this.factory);
	}

	@SuppressWarnings("unchecked")
	private Class<T> getSuperClass() {
		ParameterizedType superClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		return (Class<T>)(superClass.getActualTypeArguments().length == 0 ? SQLWrapper.class : superClass.getActualTypeArguments()[0]);
	}

	@Override
	public void init() throws InvalidConfigurationException {
		super.init();
		this.initSQL();
	}

	private void initSQL() throws InvalidConfigurationException {
		Class<T> clazz = this.getSuperClass();

		if (!SQLWrapper.class.equals(clazz)) {
			if (this.driver.equalsIgnoreCase("sql")) {
				if (PostgreSQL.class.isAssignableFrom(clazz))
					this.driver = "postgresql";
				else if (SQLServer.class.isAssignableFrom(clazz))
					this.driver = "sqlserver";
				else if (SQLServer.class.isAssignableFrom(clazz))
					this.driver = "mysql";
				else
					this.driver = clazz.getSimpleName().toLowerCase();

				if (this.port == 0) {
					try {
						Field portField = clazz.getField("DEFAULT_PORT");
						this.port = portField.getInt(null);
					} catch (Exception ex) { }
				}

				this.save();
			}
		}
	}

	protected void initFactory() throws Exception {
		if (this.driver.equalsIgnoreCase("PostgreSQL"))
			this.factory = new PostgreSQL(this.getHost(), this.getPort(), this.getUser(), this.getPass(), this.getSchema());
		else if (this.driver.equalsIgnoreCase("SQLServer"))
			this.factory = new SQLServer(this.getHost(), this.getPort(), this.getUser(), this.getPass(), this.getSchema());
		else if (this.driver.equalsIgnoreCase("MySQL"))
			this.factory = new MySQL(this.getHost(), this.getPort(), this.getUser(), this.getPass(), this.getSchema());
		else {
			Class<T> clazz = this.getSuperClass();
			Reflection sqlReflect = new Reflection(clazz.getSimpleName(), clazz.getPackage().getName());
			this.factory = (SQLWrapper)sqlReflect.newInstance(this.getHost(), this.getPort(), this.getUser(), this.getPass(), this.getSchema());
		}
	}

	@Override
	public void load() throws InvalidConfigurationException {
		super.load();

		try {
			this.initFactory();
		} catch (Exception ex) {
			throw new InvalidConfigurationException("Invalid SQL Configuration!", ex);
		}
	}

}
