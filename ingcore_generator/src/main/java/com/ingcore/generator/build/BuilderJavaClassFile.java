package com.ingcore.generator.build;


import com.ingcore.generator.config.ConfigUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class BuilderJavaClassFile {

    private String targetProject;
    private String daltargetProject;
    private String targetServer;
    private String targetImpl;
    private String projectPath;
    private String targetBean;
    private String projectPackage;
    private String targetMapper;
    private String targetServiceImpl;
    private String targetXmlMapper;
    private HashMap<String, String> _fileds;

    public BuilderJavaClassFile() {

        targetProject = ConfigUtils.readProperties("targetProject");
        daltargetProject = ConfigUtils.readProperties("daltargetProject");
        targetBean = ConfigUtils.readProperties("modelPackage");
        targetMapper = ConfigUtils.readProperties("daoMapperPackage");
        targetXmlMapper = ConfigUtils.readProperties("sqlMapperPackage");

        targetServiceImpl = ConfigUtils.readProperties("serviceImplMapperPackage");

        targetServer = ConfigUtils.readProperties("serverMapperPackage");
        targetImpl = ConfigUtils.readProperties("serviceImplMapperPackage");
        projectPackage = ConfigUtils.readProperties("projectPackage");
        projectPath = BuilderJavaClassFile.class.getResource("/").toString();
        projectPath = projectPath.substring(6);
        daltargetProject = projectPath + "../../../../" + daltargetProject;
        targetProject = projectPath + "../../../../" + targetProject;

    }

    public void delete(FileType t, String fileName) throws IOException {

        String templatePath = this.getTemplatePath(t);
        StringBuffer sb = this.readFile(projectPath, templatePath);
        String target = "";
        if (t == FileType.BEAN) {
            target = daltargetProject + targetBean.replace('.', '/') + "/" + this.getFileName(t, fileName);
        }
        if (t == FileType.BEANSEARCH) {
            target = daltargetProject + targetBean.replace('.', '/') + "/" + this.getFileName(t, fileName);
        } else if (t == FileType.MAPPER) {
            target = daltargetProject + targetMapper.replace('.', '/') + "/" + this.getFileName(t, fileName);
        } else if (t == FileType.XML) {
            target = daltargetProject + targetXmlMapper.replace('.', '/') + "/" + this.getFileName(t, fileName);
        } else if (t == FileType.IMPL) {
            target = targetProject + targetImpl.replace('.', '/') + "/" + this.getFileName(t, fileName);
        } else if (t == FileType.SERVICE) {
            target = targetProject + targetServer.replace('.', '/') + "/" + this.getFileName(t, fileName);
        }


        File f = new File(target);
        if (f.exists()) {
            f.delete();
            System.out.print("\n  delete file succeed :" + target + "");
        } else {
            System.out.print("\n  file not exists :" + target + "");
        }


    }


    public void save(FileType t, String fileName, String modelName, String keyType) throws IOException {

        String templatePath = this.getTemplatePath(t);
        StringBuffer sb = this.readFile(projectPath, templatePath);
        String target = "";
        if (t == FileType.BASE_SERVICE_IMPL) {
            target = targetProject + targetImpl.replace('.', '/') + "/" + this.getFileName(t, modelName);
        } else if (t == FileType.BASE_SERVICE) {
            target = targetProject + targetServer.replace('.', '/') + "/" + this.getFileName(t, modelName);
        } else if (t == FileType.DAO) {
            target = daltargetProject + targetMapper.replace(".base", "").replace('.', '/') + "/" + this.getFileName(t, modelName);
        } else if (t == FileType.DAO_XML) {
            target = daltargetProject + "../" + targetXmlMapper.replace(".base", "").replace('.', '/') + "/" + this.getFileName(t, modelName);
        } else if (t == FileType.SERVICE) {
            target = targetProject + targetServer.replace(".base", "").replace('.', '/') + "/" + this.getFileName(t, modelName);
        } else if (t == FileType.SERVICE_IMPL) {
            target = targetProject + targetServiceImpl.replace(".base", "").replace('.', '/') + "/" + this.getFileName(t, modelName);
        }

        String javaCode = this.assignTemplate(sb, t, fileName, modelName, keyType);


        saveFile(t, javaCode, target);
    }

    private String assignTemplate(StringBuffer sb, FileType t, String fileName, String modelName, String keyType) {
        String temp = sb.toString();
        temp = temp.replace("{package_server}", targetServer);
        temp = temp.replace("{package_bean}", targetBean);
        temp = temp.replace("{package_impl}", targetServiceImpl);
        temp = temp.replace("{bean_class_name}", modelName);
        temp = temp.replace("{bean_name}", this.getClassName("bean_name", fileName));

        if (t == FileType.BASE_SERVICE_IMPL) {
            StringBuffer criteriaCode = new StringBuffer();
            if (this._fileds != null) {
                criteriaCode.append("if (bean != null) {\n");
                Iterator<Entry<String, String>> it = this._fileds.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> entry = (Entry<String, String>) it.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    criteriaCode.append(this.assignFilesAndType(key.toString(), value.toString()));
                }
                criteriaCode.append("\t\t\tif (bean.getOrderByClause() != null) {\n");
                criteriaCode.append("\t\t\t\tsearch.setOrderByClause(bean.getOrderByClause());\n");
                criteriaCode.append("\t\t\t}\n");
                criteriaCode.append("\t\t}\n");
            }
            temp = temp.replace("{package_mapper}", targetMapper.replace(".base", ""));
            temp = temp.replace("{criteria_code}", criteriaCode.toString());
            temp = temp.replace("{cache_keys}", this.assignKeysFields());
            temp = temp.replace("{bean_timestamp_add_time}", this.assignTimestampFilelds(new String[]{"create_time", "add_time"}));
            temp = temp.replace("{bean_timestamp_update_time}", this.assignTimestampFilelds(new String[]{"updatetime", "update_time"}));
        } else if (t == FileType.DAO) {
            temp = temp.replace("{dao.package}", targetMapper.replace(".base", ""));
            temp = temp.replace("{mapper.package}", targetMapper);
        } else if (t == FileType.DAO_XML) {
            temp = temp.replace("{dao.package}", targetMapper.replace(".base", ""));
        } else if (t == FileType.SERVICE) {
            temp = temp.replace("{manager.package}", targetServer.replace(".base", ""));
            temp = temp.replace("{base.manager.package}", targetServer);
        } else if (t == FileType.SERVICE_IMPL) {
            temp = temp.replace("{manager.impl.package}", targetServiceImpl.replace(".base", ""));
            temp = temp.replace("{base.manager.impl.package}", targetServiceImpl);
            temp = temp.replace("{manager.package}", targetServer.replace(".base", ""));
        }
        temp = temp.replace("{service_key_type}", keyType);
        return temp;
    }

    private String assignTimestampFilelds(String[] fields) {
        StringBuffer sb = new StringBuffer();
        Map<String, String> fs = new HashMap<String, String>();
        if (this._fileds != null) {
            Iterator<Entry<String, String>> it = this._fileds.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = (Entry<String, String>) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                String skey = key.toString();
                String svalue = value.toString();
                for (String s : fields) {
                    if (s.equals(skey)) {
                        if (svalue.equals("datetime")) {
                            sb.append("bean.set" + this.getAttributeName(skey) + "(new Date());\n");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }


    private String assignKeysFields() {
        StringBuffer sb = new StringBuffer();
        if (this._fileds != null) {
            Iterator<Entry<String, String>> it = this._fileds.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = (Entry<String, String>) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (sb.length() > 0)
                    sb.append(",\n				");
                sb.append(this.assignKeyFilesAndType(key.toString(), value.toString()));
            }
        }
        if (sb.length() > 0)
            sb.append(",");

        sb.append("\n				Convert.toString(bean.getLimitStart()),Convert.toString(bean.getLimitEnd())");
        return sb.toString();
    }


    private String assignFilesAndType(String name, String type) {

        StringBuffer sb = new StringBuffer();
        name = this.getAttributeName(name);

        if (name != null && name.length() > 0) {
            sb.append("\t\t\tif (bean.get" + name + "() != null");
            if (type.equals("int") || type.equals("bigint") || type.equals("tinyint") || type.equals("double") || type.equals("smallint") || type.equals("float")) {
                sb.append(" && bean.get" + name + "() != -1)");
            } else if (type.equals("char") || type.equals("varchar")) {
                sb.append(" && bean.get" + name + "().length() > 0)");
            } else if (type.equals("bit") || type.equals("datetime")) {
                sb.append(")");
            } else {
                return "";
            }
            sb.append(" {\n");
            sb.append("				cra.and" + name + "EqualTo(bean.get" + name + "());\n");
            sb.append("\t\t\t}\n");
        }
        return sb.toString();
    }

    private String assignKeyFilesAndType(String name, String type) {
        StringBuffer sb = new StringBuffer();
        name = this.getAttributeName(name);
        if (name != null && name.length() > 0) {
            sb.append("	Convert.toString(bean.get" + name + "(),\"\")");
        }
        return sb.toString();
    }


    private static String getAttributeName(String name) {
        int index = name.indexOf('_');
        if (index == 0) {
            name = name.substring(1);
        } else if (index == name.length() - 1) {
            name = name.substring(0, name.length() - 1);
        } else if (index != -1) {
            name = name.substring(0, index) + name.substring(index + 1, index + 2).toUpperCase() + name.substring(index + 2);
        }
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        index = name.indexOf('_');
        if (index > 0) {
            name = getAttributeName(name);
        }
        return name;
    }

    private void saveFile(FileType t, String sb, String fileName) throws IOException {
        String parentDir = fileName.substring(0, fileName.lastIndexOf("/"));
        File dir = new File(parentDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(fileName);
        if (file.exists()) {
            if (t == FileType.DAO || t == FileType.DAO_XML || t == FileType.SERVICE || t == FileType.SERVICE_IMPL) {
                return;
            }
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
        osw.write(sb.toString());
        osw.flush();
        osw.close();
    }

    private String getTemplatePath(FileType t) {
        if (t == FileType.JUNITTEST) {
            return "/template/JUnitTemplate.template";
        } else if (t == FileType.SERVICE_IMPL) {
            return "/template/ManagerImplTemplate.template";
        } else if (t == FileType.SERVICE) {
            return "/template/ManagerTemplate.template";
        } else if (t == FileType.DAO_XML) {
            return "/template/DaoXmlTemplate.template";
        } else if (t == FileType.DAO) {
            return "/template/DaoTemplate.template";
        } else if (t == FileType.BASE_SERVICE) {
            return "/template/BaseManagerTemplate.template";
        } else if (t == FileType.BASE_SERVICE_IMPL) {
            return "/template/BaseManagerImplTemplate.template";
        }
        return "/template/ServiceTemplate.template";
    }

    private String getFileName(FileType t, String fileName) {
        fileName = this.getAttributeName(fileName);

        if (t == FileType.JUNITTEST) {
            return fileName + "ManagerTest.java";
        } else if (t == FileType.BASE_SERVICE) {
            return "Base" + fileName + "Manager.java";
        } else if (t == FileType.BASE_SERVICE_IMPL) {
            return "Base" + fileName + "ManagerImpl.java";
        } else if (t == FileType.BEAN) {
            return fileName + ".java";
        } else if (t == FileType.BEANSEARCH) {
            return fileName + "Search.java";
        } else if (t == FileType.XML) {
            return "Base" + fileName + "Mapper.xml";
        } else if (t == FileType.MAPPER) {
            return "Base" + fileName + "Mapper.java";
        } else if (t == FileType.DAO) {
            return fileName + "Dao.java";
        } else if (t == FileType.DAO_XML) {
            return fileName + "Dao.xml";
        } else if (t == FileType.SERVICE) {
            return fileName + "Manager.java";
        } else if (t == FileType.SERVICE_IMPL) {
            return fileName + "ManagerImpl.java";
        }
        return "";
    }

    private String getClassName(String t, String fileName) {
        fileName = this.getAttributeName(fileName);
        if (t == "bean_class_name") {
            return fileName;
        }
        if (t == "bean_name") {
            return fileName.substring(0, 1).toLowerCase() + fileName.substring(1);
        }
        return fileName;
    }

    private StringBuffer readFile(String projectPath, String templatePath)
            throws IOException {

        StringBuffer sb = new StringBuffer();
        FileReader fr = new FileReader(projectPath + templatePath);
        int ch = 0;
        try {
            while ((ch = fr.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }

        return sb;
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    public String getTargetImpl() {
        return targetImpl;
    }

    public void setTargetImpl(String targetImpl) {
        this.targetImpl = targetImpl;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getTargetBean() {
        return targetBean;
    }

    public void setTargetBean(String targetBean) {
        this.targetBean = targetBean;
    }

    public String getProjectPackage() {
        return projectPackage;
    }

    public void setProjectPackage(String projectPackage) {
        this.projectPackage = projectPackage;
    }

    public String getTargetMapper() {
        return targetMapper;
    }

    public void setTargetMapper(String targetMapper) {
        this.targetMapper = targetMapper;
    }

    public String getTargetServiceImpl() {
        return targetServiceImpl;
    }

    public void setTargetServiceImpl(String targetServiceImpl) {
        this.targetServiceImpl = targetServiceImpl;
    }

    public String getTargetXmlMapper() {
        return targetXmlMapper;
    }

    public void setTargetXmlMapper(String targetXmlMapper) {
        this.targetXmlMapper = targetXmlMapper;
    }

    public HashMap<String, String> get_fileds() {
        return _fileds;
    }

    public void set_fileds(HashMap<String, String> _fileds) {
        this._fileds = _fileds;
    }

    public String getDaltargetProject() {
        return daltargetProject;
    }

    public void setDaltargetProject(String daltargetProject) {
        this.daltargetProject = daltargetProject;
    }
}

