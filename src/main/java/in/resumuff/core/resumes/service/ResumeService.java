package in.resumuff.core.resumes.service;

import in.resumuff.core.comments.logic.CommentService;
import in.resumuff.core.resumes.entity.Resume;
import in.resumuff.core.resumes.repository.ResumeRepository;
import in.resumuff.core.users.logic.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository repository;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    public Optional<Resume> storeResume(long uid, MultipartFile file, String[] tags, String title, String description) {
        String contentType = file.getContentType();
        if (contentType == null)
            return Optional.empty();

        if (!tagService.validTags(convertTags(tags)))
            return Optional.empty();

        try {
            Resume resume = new Resume(uid, file.getBytes(), contentType.startsWith("image/"), convertTags(tags), title,
                    description);
            return Optional.of(repository.save(resume));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return Optional.empty();
    }

    int[] convertTags(String[] tags) {
        int[] result = new int[tags.length];
        try {
            for (int i = 0; i < tags.length; ++i)
                result[i] = Integer.parseInt(tags[i]);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return result;

    }

    public Optional<Resume> getResume(long id) {
        return repository.findById(id);
    }

    public Iterable<Resume> getAllResumes() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Resume> getResumes(int pageNum, int pageLen) {
        Pageable pageable = PageRequest.of(pageNum, pageLen);
        Page<Resume> resumes = repository.findAll(pageable);
        return resumes;
    }

    @Transactional
    public void deleteResume(HttpSession session, long id) {
        if(userService.hasDeleteAbility(session)) {
            repository.deleteById(id);
            commentService.deleteCommentsRecursive(id);
        }
        
    }

}
