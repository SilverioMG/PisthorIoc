package pisthorioc.test.dependencies;

public class ServiceCircular3 {

    private ServiceCircular3 serviceCircular3;

    public ServiceCircular3(ServiceCircular3 serviceCircular3){
        this.serviceCircular3 = serviceCircular3;
    }
}
