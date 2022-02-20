package pisthorioc.test.dependencies;


public class ServiceCircular1 {

    private ServiceCircular2 serviceCircular2;

    public ServiceCircular1 (ServiceCircular2 serviceCircular2){
        this.serviceCircular2 = serviceCircular2;
    }

    public ServiceCircular2 getServiceCircular2() {
        return serviceCircular2;
    }
}
