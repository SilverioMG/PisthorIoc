package pisthorioc.test.dependencies;


import pisthorioc.test.dependencies.interfaces.IRepository;
import pisthorioc.test.dependencies.interfaces.IService;

import java.util.List;

public class Service2 implements IService {

    private IRepository repository1;
    private IRepository repository2;

    public Service2(IRepository repository1, IRepository repository2){
        this.repository1 = repository1;
        this.repository2 = repository2;
    }

    @Override
    public String getMessageService() {
        return "Service2";
    }

    @Override
    public List<IRepository> getRepositories() {
        return List.of(repository1, repository2);
    }
}
