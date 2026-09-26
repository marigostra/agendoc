module agendoc
{
    requires langchain4j;
    requires javafx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;
    requires java.net.http;
    exports agendoc;
    exports agendoc.docs;
}
