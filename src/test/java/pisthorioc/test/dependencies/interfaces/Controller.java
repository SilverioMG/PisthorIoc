package pisthorioc.test.dependencies.interfaces;

public class Controller {

    private IRepository repository1;
    private IService service1;
    private IService service2;

    public Controller(IRepository repository1, IService service1, IService service2){
        this.repository1 = repository1;
        this.service1 = service1;
        this.service2 = service2;
    }

    public IRepository getRepository1() {
        return repository1;
    }

    public IService getService1() {
        return service1;
    }

    public IService getService2() {
        return service2;
    }
}
