package org.example.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAllReturnsEveryCategory() {
        List<Category> categories = List.of(new Category("Bebidas", null), new Category("Snacks", null));
        when(categoryRepository.findAll()).thenReturn(categories);

        assertThat(categoryService.findAll()).containsExactlyElementsOf(categories);
    }

    @Test
    void findByIdReturnsCategory() {
        Category category = new Category("Bebidas", "Sin alcohol");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(categoryService.findById(1L)).isSameAs(category);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void createSavesCategoryWhenNameIsFree() {
        when(categoryRepository.existsByName("Bebidas")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category created = categoryService.create("Bebidas", "Sin alcohol");

        assertThat(created.getName()).isEqualTo("Bebidas");
        assertThat(created.getDescription()).isEqualTo("Sin alcohol");
        verify(categoryRepository).save(created);
    }

    @Test
    void createThrowsWhenNameAlreadyExists() {
        when(categoryRepository.existsByName("Bebidas")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("Bebidas", null))
                .isInstanceOf(DuplicateCategoryNameException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateChangesFieldsWhenNameIsUnchanged() {
        Category category = new Category("Bebidas", "Viejo");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category updated = categoryService.update(1L, "Bebidas", "Nuevo");

        assertThat(updated.getDescription()).isEqualTo("Nuevo");
        verify(categoryRepository, never()).existsByName(any());
    }

    @Test
    void updateRenamesWhenNewNameIsFree() {
        Category category = new Category("Bebidas", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Jugos")).thenReturn(false);

        Category updated = categoryService.update(1L, "Jugos", null);

        assertThat(updated.getName()).isEqualTo("Jugos");
    }

    @Test
    void updateThrowsWhenRenamingToExistingName() {
        Category category = new Category("Bebidas", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Snacks")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, "Snacks", null))
                .isInstanceOf(DuplicateCategoryNameException.class);
        assertThat(category.getName()).isEqualTo("Bebidas");
    }

    @Test
    void updateThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, "Bebidas", null))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingCategory() {
        Category category = new Category("Bebidas", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryRepository, never()).delete(any());
    }
}
