package wang.study.designpattern.factorypattern;

/**
 * 工厂模式
 * 优点：
 *  1、一个调用者想创建一个对象，只要知道其名称就可以了。
 *  2、扩展性高，如果想增加一个产品，只要扩展一个工厂类就可以。
 *  3、屏蔽产品的具体实现，调用者只关心产品的接口。
 *
 * 缺点：
 *  每次增加一个产品时，都需要增加一个具体类和对象实现工厂，使得系统中类的个数成倍增加，
 *  在一定程度上增加了系统的复杂度，同时也增加了系统具体类的依赖。这并不是什么好事。
 *
 *
 * 抽象工厂模式
 *    围绕着一个超级工厂创建其他工厂
 * 优点：当一个产品族中的多个对象被设计成一起工作时，它能保证客户端始终只使用同一个产品族中的对象。
 * 缺点：产品族扩展非常困难，要增加一个系列的某一产品，既要在抽象的 Creator 里加代码，又要在具体的里面加代码。
 */
public class ShapeFactory {
    public Shape getShape(String shapeType) {
        if (shapeType == null) {
            return null;
        }
        if (shapeType.equalsIgnoreCase("Circle")) {
            return new Circle();
        } else if (shapeType.equalsIgnoreCase("Rectangle")) {
            return new Rectangle();
        }
        return null;
    }
}
