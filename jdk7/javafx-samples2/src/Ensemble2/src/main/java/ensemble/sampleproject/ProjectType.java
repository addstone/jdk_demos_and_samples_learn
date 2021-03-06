package ensemble.sampleproject;

/**
 * Description:  Netbeans ProjectType
 * Author: LuDaShi
 * Date: 2021-01-05-15:23
 * UpdateDate: 2021-01-05-15:23
 * FileName: ProjectType
 * Version: 0.0.0.1
 * Since: 0.0.0.1
 */
public class ProjectType {

    public static final int UNKNOWN_TYPE = 1;
    public static final int J2SE_TYPE = 2;
    public static final int FREEFORM_TYPE = 3;
    public static final int J2ME_TYPE = 4;
    public static final int WEB_TYPE = 5;
    public static final int EJB_TYPE = 6;
    public static final int EAR_TYPE = 7;
    public static final int MAVEN_TYPE = 7;

    public static final String J2SE_NAME = "org.netbeans.modules.java.j2seproject";
    public static final String FREEFORM_NAME = "org.netbeans.modules.ant.freeform";
    public static final String J2ME_NAME = "org.netbeans.modules.kjava.j2meproject";
    public static final String WEB_NAME = "org.netbeans.modules.web.project";
    public static final String EJB_NAME = "org.netbeans.modules.j2ee.ejbjarproject";
    public static final String EAR_NAME = "org.netbeans.modules.j2ee.earproject";
    public static final String MAVEN_NAME = "maven";

    public static final ProjectType J2SE = new J2SEProjectType();
    public static final ProjectType FREEFORM = new FreeformProjectType();
    public static final ProjectType J2ME = new J2MEProjectType();
    public static final ProjectType WEB = new WebProjectType();
    public static final ProjectType EJB = new EJBProjectType();
    public static final ProjectType EAR = new EARProjectType();
    public static final ProjectType MAVEN = new MavenProjectType();

    private String typeString;
    private String[] importantFiles;

    public ProjectType(String type, String[] impFiles) {
        typeString = type;
        importantFiles = impFiles;
    }

    public String getTypeString() {
        return typeString;
    }

    public String[] getImportantFiles() {
        return importantFiles;
    }

    @Override
    public String toString() {
        return getTypeString();
    }

    // ---

    public static final class J2SEProjectType extends ProjectType {

        public J2SEProjectType() {
            super(J2SE_NAME, new String[]{"modules/org-netbeans-modules-java-j2seproject.jar"});
        }
    }

    public static final class FreeformProjectType extends ProjectType {

        public FreeformProjectType() {
            super(FREEFORM_NAME, new String[]{"modules/org-netbeans-modules-ant-freeform.jar"});
        }
    }

    public static final class J2MEProjectType extends ProjectType {

        public J2MEProjectType() {
            super(J2ME_NAME, new String[]{"modules/org-netbeans-modules-kjava-j2meproject.jar"});
        }
    }

    public static final class WebProjectType extends ProjectType {

        public WebProjectType() {
            super(WEB_NAME, new String[]{"modules/org-netbeans-modules-web-project.jar"});
        }
    }

    public static final class EJBProjectType extends ProjectType {

        public EJBProjectType() {
            super(EJB_NAME, new String[]{"modules/org-netbeans-modules-j2ee-ejbjarproject.jar"});
        }
    }

    public static final class EARProjectType extends ProjectType {

        public EARProjectType() {
            super(EAR_NAME, new String[]{"modules/org-netbeans-modules-j2ee-earproject.jar"});
        }
    }

    public static final class MavenProjectType extends ProjectType {

        public MavenProjectType() {
            super(MAVEN_NAME, new String[]{"modules/org-codehaus-mevenide-netbeans.jar"});
        }
    }

}
