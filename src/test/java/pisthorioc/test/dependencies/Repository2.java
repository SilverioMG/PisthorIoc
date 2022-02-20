package pisthorioc.test.dependencies;


import pisthorioc.test.dependencies.interfaces.IRepository;

public class Repository2 implements IRepository {

    @Override
    public String getMessageRepository() {
        return "Repository2";
    }
}
