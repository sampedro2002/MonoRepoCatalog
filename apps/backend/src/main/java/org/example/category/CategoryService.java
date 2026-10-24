package org.example.category;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public Category create(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new DuplicateCategoryNameException(name);
        }
        return categoryRepository.save(new Category(name, description));
    }

    public Category update(Long id, String name, String description) {
        Category category = findById(id);
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new DuplicateCategoryNameException(name);
        }
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }
}
