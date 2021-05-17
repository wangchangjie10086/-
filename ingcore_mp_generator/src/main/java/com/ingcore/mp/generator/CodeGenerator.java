package com.ingcore.mp.generator;


import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.ingcore.mp.generator.config.ConfigUtils;
import com.ingcore.mp.generator.config.StringPool;

import java.util.ArrayList;
import java.util.List;

/**
 * mybatis-plus 代码生成器
 * https://mp.baomidou.com/config/generator-config.html#swagger2
 */
public class CodeGenerator {

    private static final String DOT_VM = ".vm";
    private static final String EXT_MAPPER_XML = "/template/dao.xml";
    private static String packageName = ConfigUtils.readProperties("packageName");


    public static void main(String[] args) {
        generateByTables(true, "t_card_promotion_sale");
    }

    /**
     * 生成文件
     *
     * @param create     true 创建  false 更新
     * @param tableNames 表名集合
     */
    private static void generateByTables(boolean create, String... tableNames) {

        // 项目根目录
        String projectPath = System.getProperty("user.dir");

        // 全局策略配置
        GlobalConfig config = new GlobalConfig();
        config.setOutputDir(projectPath + "/" + ConfigUtils.readProperties("daltargetProject"))
                .setFileOverride(true)
                .setOpen(false)
                .setEnableCache(false)
                .setAuthor("admin")
                .setKotlin(false)
//                .setSwagger2(false)
                .setActiveRecord(false)
                .setBaseResultMap(true)
                .setBaseColumnList(true)
//                .setDateType(DateType.ONLY_DATE)
//                .setEntityName(null)
                .setMapperName("%sDao")
                .setXmlName("%sMapper")
                .setServiceName("%sService")
                .setServiceImplName("%sServiceImpl")
                .setControllerName("%sController")
                .setIdType(IdType.UUID);

        // 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setUrl(ConfigUtils.readProperties("connectionURL"))
                .setUsername(ConfigUtils.readProperties("userId"))
                .setPassword(ConfigUtils.readProperties("password"))
                .setDriverName(ConfigUtils.readProperties("driverClass"));

        // 数据库表策略配置
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setCapitalMode(true)
                .setEntityLombokModel(false)
                .setNaming(NamingStrategy.underline_to_camel)
                .setTablePrefix("t_")
                .setInclude(tableNames);


        // 包名配置
        PackageConfig packageConfig = new PackageConfig()
                .setParent(packageName)
//                .setModuleName("user")
                .setController("controller")
                .setEntity(ConfigUtils.readProperties("modelPackage"))
                .setService(ConfigUtils.readProperties("servicePackage"))
                .setMapper(ConfigUtils.readProperties("baseDaoPackage"))
                .setServiceImpl(ConfigUtils.readProperties("serviceImplPackage"));

        // 模板配置
        TemplateConfig templateConfig = new TemplateConfig();

        // 自定义配置
        InjectionConfig injectionConfig = new InjectionConfig() {
            @Override
            public void initMap() {

            }
        };
        // 自定义输出配置,自定义配置会被优先输出
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig(templateConfig.getXml() + DOT_VM) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return projectPath + "/" + ConfigUtils.readProperties("dalMapperTargetProject")
                        + "Base" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        // 只有是新表时才需要创建dao.java,dao.xml,service.java,serviceImpl.java
        if (create) {
            focList.add(new FileOutConfig(EXT_MAPPER_XML + DOT_VM) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return projectPath + "/" + ConfigUtils.readProperties("dalMapperTargetProject").replace("/base", "")
                            + tableInfo.getEntityName() + "Dao" + StringPool.DOT_XML;
                }
            });
            focList.add(new FileOutConfig(templateConfig.getMapper() + DOT_VM) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return projectPath + "/" + ConfigUtils.readProperties("daltargetProject") + "/" +
                            packageName.replace(".", "/") + "/" +
                            ConfigUtils.readProperties("baseDaoPackage").replace(".", "/")
                            + "/" + tableInfo.getEntityName() + "Dao" + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig(templateConfig.getService() + DOT_VM) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return projectPath + "/" + ConfigUtils.readProperties("targetProject") +
                            packageName.replace(".", "/") + "/" +
                            ConfigUtils.readProperties("servicePackage").replace(".", "/")
                            + "/" + tableInfo.getEntityName() + "Service" + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig(templateConfig.getServiceImpl() + DOT_VM) {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return projectPath + "/" + ConfigUtils.readProperties("targetProject") +
                            packageName.replace(".", "/") + "/" +
                            ConfigUtils.readProperties("serviceImplPackage").replace(".", "/")
                            + "/" + tableInfo.getEntityName() + "ServiceImpl" + StringPool.DOT_JAVA;
                }
            });
        }
        // 设置自定义输出目录
        injectionConfig.setFileOutConfigList(focList);

        // 禁用默认模板生成策略
        templateConfig.setController(null)
                .setService(null)
                .setServiceImpl(null)
                .setXml(null)
                .setMapper(null);

        //配置生成器执行生成
        new AutoGenerator().setGlobalConfig(config)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(packageConfig)
                .setTemplate(templateConfig)
                .setCfg(injectionConfig)
                .execute();
    }

}
