describe('Service: bindingService', function () {

  beforeEach(module('bonitasoft.ui.services'));

  let bindingService, property, context;

  beforeEach(inject(function (_bindingService_) {
    bindingService = _bindingService_;
    property = {};
    context = {};
  }));

  it('should create a constant binding for a constant property', function () {
    property.type = 'constant';
    property.value = 'hello';

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('ConstantBinding');
    expect(binding.getValue()).toBe('hello');
  });

  it('should create an interpolation binding for an interpolated property', function () {
    property.type = 'interpolation';
    property.value = '{{ variable }}';
    context.variable = 'hello';

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('InterpolationBinding');
    expect(binding.getValue()).toBe('hello');
  });

  it('should create an expression binding for an expression property', function () {
    property.type = 'expression';
    property.value = 'a + b';
    context.a = 3;
    context.b = 4;

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('ExpressionBinding');
    expect(binding.getValue()).toBe(7);
  });

  it('should create a non readable variable binding for a variable property', function () {
    property.type = 'variable';
    property.value = '';

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('VariableBinding');
    expect(binding.getValue()).toBeUndefined();

    binding.setValue('123');
    expect(binding.getValue()).toBe('123');

  });

  it('should create a readable but not writeable variable binding for a variable property', function () {
    property.type = 'variable';
    property.value = '!foobar';

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('VariableBinding');
    expect(binding.getValue()).toBeTruthy();
    context.foobar = 'hello';
    expect(binding.getValue()).toBeFalsy();
  });

  it('should create a readable variable binding for a variable property', function () {
    property.type = 'variable';
    property.value = 'foobar';
    context.foobar = 'hello';

    let binding = bindingService.create(property, context);

    expect(binding.constructor.name).toBe('VariableBinding');
    expect(binding.getValue()).toBe('hello');
  });

  it('should create a writable variable binding for a variable property', function () {
    property.type = 'variable';
    property.value = 'foobar';
    context.foobar = 'hello';

    let binding = bindingService.create(property, context);
    binding.setValue('bonjour');

    expect(binding.constructor.name).toBe('VariableBinding');
    expect(binding.getValue()).toBe('bonjour');
  });

  it('should throw an error when property type is unknown', function () {
    property.type = 'unknown';

    expect(function() {
      bindingService.create(property, context);
    }).toThrowError();
  });
});
