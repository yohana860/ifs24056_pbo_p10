package org.delcom.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.delcom.app.entities.Todo;
import org.delcom.app.repositories.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TodoServiceTests {
    @Test
    @DisplayName("Pengujian untuk service Todo")
    void testTodoService() throws Exception {
        // Buat random UUID
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        UUID nonexistentTodoId = UUID.randomUUID();

        // Membuat dummy data
        Todo todo = new Todo(userId, "Belajar Spring Boot", "Belajar mock repository di unit test", false);
        todo.setId(todoId);

        // Membuat mock TodoRepository
        // Buat mock
        TodoRepository todoRepository = Mockito.mock(TodoRepository.class);

        // Atur perilaku mock
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);
        when(todoRepository.findByKeyword(userId, "Belajar")).thenReturn(java.util.List.of(todo));
        when(todoRepository.findAll()).thenReturn(java.util.List.of(todo));
        when(todoRepository.findByUserIdAndId(userId, todoId)).thenReturn(java.util.Optional.of(todo));
        when(todoRepository.findByUserIdAndId(userId, nonexistentTodoId)).thenReturn(java.util.Optional.empty());
        when(todoRepository.existsById(todoId)).thenReturn(true);
        when(todoRepository.existsById(nonexistentTodoId)).thenReturn(false);
        doNothing().when(todoRepository).deleteById(any(UUID.class));

        // Membuat instance service
        TodoService todoService = new TodoService(todoRepository);
        assert (todoService != null);

        // Menguji create todo
        {
            Todo createdTodo = todoService.createTodo(userId, todo.getTitle(), todo.getDescription());
            assert (createdTodo != null);
            assert (createdTodo.getId().equals(todoId));
            assert (createdTodo.getTitle().equals(todo.getTitle()));
            assert (createdTodo.getDescription().equals(todo.getDescription()));
        }

        // Menguji getAllTodos
        {
            var todos = todoService.getAllTodos(userId, null);
            assert (todos.size() == 1);
        }

        // Menguji getAllTodos dengan pencarian
        {
            var todos = todoService.getAllTodos(userId, "Belajar");
            assert (todos.size() == 1);

            todos = todoService.getAllTodos(userId, "     ");
            assert (todos.size() == 1);
        }

        // Menguji getTodoById
        {

            Todo fetchedTodo = todoService.getTodoById(userId, todoId);
            assert (fetchedTodo != null);
            assert (fetchedTodo.getId().equals(todoId));
            assert (fetchedTodo.getTitle().equals(todo.getTitle()));
            assert (fetchedTodo.getDescription().equals(todo.getDescription()));
        }

        // Menguji getTodoById dengan ID yang tidak ada
        {
            Todo fetchedTodo = todoService.getTodoById(userId, nonexistentTodoId);
            assert (fetchedTodo == null);
        }

        // Menguji updateTodo
        {
            String updatedTitle = "Belajar Spring Boot Lanjutan";
            String updatedDescription = "Belajar mock repository di unit test dengan Mockito";
            Boolean updatedIsFinished = true;

            Todo updatedTodo = todoService.updateTodo(userId, todoId, updatedTitle, updatedDescription,
                    updatedIsFinished);
            assert (updatedTodo != null);
            assert (updatedTodo.getTitle().equals(updatedTitle));
            assert (updatedTodo.getDescription().equals(updatedDescription));
            assert (updatedTodo.isFinished() == updatedIsFinished);
        }

        // Menguji update Todo dengan ID yang tidak ada
        {
            String updatedTitle = "Belajar Spring Boot Lanjutan";
            String updatedDescription = "Belajar mock repository di unit test dengan Mockito";
            Boolean updatedIsFinished = true;

            Todo updatedTodo = todoService.updateTodo(userId, nonexistentTodoId, updatedTitle, updatedDescription,
                    updatedIsFinished);
            assert (updatedTodo == null);
        }

        // Menguji deleteTodo
        {
            boolean deleted = todoService.deleteTodo(userId, todoId);
            assert (deleted == true);
        }

        // Menguji deleteTodo dengan ID yang tidak ada
        {
            boolean deleted = todoService.deleteTodo(userId, nonexistentTodoId);
            assert (deleted == false);
        }
    }
}