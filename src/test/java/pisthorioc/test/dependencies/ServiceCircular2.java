package pisthorioc.test.dependencies;


public class ServiceCircular2 {

    private ServiceCircular1 serviceCircular1;

    public ServiceCircular2(ServiceCircular1 serviceCircular1){
        this.serviceCircular1 = serviceCircular1;
    }

    public ServiceCircular1 getServiceCircular1() {
        return serviceCircular1;
    }
}
